Ext.define('global.classes.Utils', {
    alternateClassName: 'MgsUtils',	
	statics:{
		errorMsg: function(message){
			Ext.Msg.alert('Ошибка', message);
		}
	}
});
