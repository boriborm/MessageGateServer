Ext.define('opers.store.Messages', {
	extend: 'global.store.Store',	
	model: 'opers.model.Message',
	storeId: 'messagesStore',
	sorters:{property:"createDate", direction:"ASC"},
	remoteSort:true,
	remoteFilter:true
});
