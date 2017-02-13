Ext.define('opers.model.Channel', {
    extend: 'Ext.data.Model',	
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/channels",
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
	idProperty: 'channel',
    fields: [
		{name:'channel', type:'string'},
		{name:'channelDescription', type:'string'}
    ]
});
