Ext.define('opers.model.PhoneGrant', {
    extend: 'Ext.data.Model',
	proxy: {
		type: 'rest',
		url: "/rest/phonegrants",
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
	idProperty: 'phoneNumber',
    fields: [
		{name:'phoneNumber', type:'string'},
		{name:'acceptSms', type:'boolean'},
		{name:'acceptViber', type:'boolean'},
		{name:'acceptVoice', type:'boolean'},
		{name:'acceptParseco', type:'boolean'}
    ]
});
