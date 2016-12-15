Ext.define('opers.model.Bulk', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/bulks",
		reader:{type:'json', rootProperty:'data'},
		listeners : {
			exception : function(proxy, response, operation) {
				try {
					var jResponse = Ext.decode(response.responseText);
					Ext.Msg.alert('Ошибка', jResponse.message);
				} catch (err){
					Ext.Msg.alert('Ошибка', response.statusText);
				}
			}
		}
	},
	idProperty: 'id',
    fields: [
		{name:'id', type:'auto'},
		{name:'description', type:'string'}
    ]
});
