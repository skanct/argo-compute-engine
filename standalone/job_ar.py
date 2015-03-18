#!/usr/bin/env python

# arg parsing related imports
import os, sys, json
from datetime import datetime, timedelta
from subprocess import call
from argparse import ArgumentParser
from ConfigParser import SafeConfigParser

def main(args=None):

	# default config 
	fn_ar_cfg = "/etc/ar-compute-engine.conf"
	arsync_exec = "/usr/libexec/ar-sync/"
	arsync_lib = "/var/lib/ar-sync/"
	arcomp_conf = "/etc/ar-compute/"
	arcomp_exec = "/usr/libexec/ar-compute/"
	stdl_exec = "/usr/libexec/ar-compute/standalone"
	pig_script_path = "/usr/libexec/ar-compute/pig/"

	actual_date = datetime.strptime(args.date,'%Y-%m-%d')
	one_day_ago = actual_date - timedelta(days=1)
	prev_date = one_day_ago.strftime('%Y-%m-%d')
	prev_date_under = prev_date.replace("-","_")

	date_under = args.date.replace("-","_")

	ArConfig = SafeConfigParser()
	ArConfig.read(fn_ar_cfg)

	mongo_host = ArConfig.get('default','mongo_host')
	mongo_port = ArConfig.get('default','mongo_port')
	mongo_dest_service = ArConfig.get('datastore_mapping','service_dest')
	mongo_dest_egroup = ArConfig.get('datastore_mapping','egroup_dest') 
	ar_mode = ArConfig.get('default','mode')

	# check if sync_data must be cleaned in hdfs
	sync_clean = ArConfig.get('default','sync_clean');	

	# Proposed hdfs pathways
	hdfs_mdata_path = './' + args.tenant + "/mdata/"
	hdfs_sync_path = './scratch/sync/' + args.tenant + "/" + args.job + "/" + date_under + "/" 

	# Proposed local pathways
	local_mdata_path = arsync_lib
	local_sync_path = arsync_lib + args.tenant + "/" + args.job + "/"
	local_cfg_path = arcomp_conf 

	if ar_mode == 'cluster':
		mode = 'cache'
		mdata_path = hdfs_mdata_path
		sync_path = hdfs_sync_path
		root_sync_path = hdfs_sync_path
		cfg_path = hdfs_sync_path
		
	else:
		mode = 'local'
		mdata_path = local_mdata_path
		sync_path = local_sync_path
		root_sync_path = arsync_lib
		cfg_path = local_cfg_path
		
	# open job configuration file
	json_cfg_file=open(local_cfg_path+args.tenant + "_" + args.job + "_cfg.json");
	json_cfg=json.load(json_cfg_file)

	# dictionary with necessary pig parameters
	pig_params={}

	pig_params['mdata'] = mdata_path + 'prefilter_' + date_under + '.avro';
	pig_params['p_mdata'] = mdata_path + 'prefilter_' + prev_date_under + '.avro';
	pig_params['egs'] = sync_path + 'group_endpoints.avro';
	pig_params['ggs'] = sync_path + 'group_groups.avro';
	pig_params['mps'] = sync_path + 'poem_sync.avro';
	pig_params['dts'] = root_sync_path + 'downtimes.avro'; 
	pig_params['weight'] = sync_path + 'weights_sync.avro';
	pig_params['aps'] = cfg_path + args.tenant + '_' + args.job + '_ap.json';
	pig_params['ops'] = cfg_path + args.tenant + '_ops.json';
	pig_params['cfg'] = cfg_path + args.tenant + '_' + args.job + '_cfg.json';
	pig_params['rec'] = cfg_path + args.tenant + '_recalc.json';
	pig_params['localCfg'] = local_cfg_path + args.tenant + "_" + args.job + '_cfg.json'
	pig_params['dt'] = args.date 
	pig_params['mode'] = mode
	pig_params['name_eg'] = json_cfg['egroup']
	pig_params['name_sg'] = json_cfg['ggroup']
	pig_params['n_alt'] = ArConfig.get('datastore_mapping','n_alt')
	pig_params['s_period'] = ArConfig.get('sampling','s_period') 
	pig_params['s_interval'] = ArConfig.get('sampling','s_interval')
	pig_params['e_map'] = ArConfig.get('datastore_mapping','e_map')
	pig_params['s_map'] = ArConfig.get('datastore_mapping','s_map')
	pig_params['flt'] = '1'
	pig_params['mongo_service'] = 'mongodb://' + mongo_host + ':' + mongo_port + '/' + mongo_dest_service;
	pig_params['mongo_egroup'] = 'mongodb://' + mongo_host + ':' + mongo_port + '/' + mongo_dest_egroup;

	cmd_pig = []

	# Append pig command
	cmd_pig.append('pig')

	# Append Pig local execution mode flag
	if ar_mode == 'local':
		cmd_pig.append('-x')
		cmd_pig.append('local')

	# Append Pig Parameters
	for item in pig_params:
		cmd_pig.append('-param')
		cmd_pig.append(item+'='+pig_params[item])

	# Append Pig Executionable Script
	cmd_pig.append('-f')
	cmd_pig.append(pig_script_path+'compute-ar.pig')

	# Command to clean a/r data from mongo
	cmd_clean_mongo_ar = [os.path.join(stdl_exec,"mongo_clean_ar.py"),'-d',args.date,'-p',json_cfg["aprofile"]]

	# Command to upload sync data to hdfs
	cmd_upload_sync = [os.path.join(stdl_exec,"upload_sync.py"),'-d',args.date,'-t',args.tenant,'-j',args.job]

	# Command to clean hdfs data
	cmd_clean_sync = ['hadoop','fs','-rm','-r',hdfs_sync_path]

	# Upload data to hdfs
	print "Uploading sync data to hdfs..."
	call(cmd_upload_sync)

	# Clean data from mongo
	print "Cleaning data from mongodb"
	call(cmd_clean_mongo_ar)

	# Call pig
	print "Submitting pig compute a/r job..."
	call(cmd_pig)

	# Cleaning hdfs sync data 
	if sync_clean == "true":
		print "System configured to clean sync hdfs data after job"
		call(cmd_clean_sync)

	print "Execution of ar job %s for tenant %s for date %s completed!" % (args.job , args.tenant, args.date)

if __name__ == "__main__":

	# Feed Argument parser with the description of the 3 arguments we need (input_file,output_file,schema_file)
	arg_parser = ArgumentParser(description="Initiate an a/r computation job")
	arg_parser.add_argument("-d","--date",help="date", dest="date", metavar="DATE", required="TRUE")
	arg_parser.add_argument("-t","--tenant",help="tenant owner ", dest="tenant", metavar= "STRING", required="TRUE")
	arg_parser.add_argument("-j","--job",help="job name ", dest="job", metavar= "STRING", required="TRUE")
	# Parse the command line arguments accordingly and introduce them to main...
	sys.exit(main(arg_parser.parse_args()))