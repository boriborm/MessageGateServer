Ext.define('global.store.Store', {
	extend: 'Ext.data.Store',
	isLoadedSuccessful:false,
	listeners:{
		load: function(store, records, successful, eOpts){
			store.isLoadedSuccessful = successful;
		}
	},
	autoLoad:false,
	autoSync:false,
	pageSize:50
});
