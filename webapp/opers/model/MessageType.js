Ext.define('opers.model.MessageType', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		extraParams:{active:'Y'},
		url: "/rest/messagetypes",
		reader:{type:'json', rootProperty:'data' /*, totalProperty:'results', messageProperty:'error.reason'*/},
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
	idProperty: 'typeId',
    fields: [
		{name:'typeId', type:'auto'},
		{name:'description', type:'string'},
		{name:'acceptSms', type:'bool'},
		{name:'acceptViber', type:'bool'},
		{name:'acceptVoice', type:'bool'},
		{name:'acceptParseco', type:'bool'},
		{name:'acceptEmail', type:'bool'}
    ]
});
