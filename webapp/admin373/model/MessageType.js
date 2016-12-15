Ext.define('admins.model.MessageType', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/messagetypes",
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
		},
		actionMethods:{ create: 'POST', read: 'GET', update: 'PUT', destroy: 'DELETE' },
		writer:{type:'json', writeAllFields:true}
	},
	idProperty: 'typeId',
    fields: [
		{name:'typeId', type:'auto'},
		{name:'description', type:'string'},
		{name:'acceptSms', type:'bool'},
		{name:'acceptViber', type:'bool'},
		{name:'acceptVoice', type:'bool'},
		{name:'acceptParseco', type:'bool'},
		{name:'acceptEmail', type:'bool'},
		{name:'active', type:'bool'},
		{name:'smsValidityPeriod', type:'number'},
		{name:'viberValidityPeriod', type:'number'},
		{name:'voiceValidityPeriod', type:'number'},
		{name:'parsecoValidityPeriod', type:'number'}
		
    ]
});
