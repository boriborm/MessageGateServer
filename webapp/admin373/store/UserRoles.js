Ext.define('admins.store.UserRoles', {
	extend: 'Ext.data.ArrayStore',	
	storeId:'userRolesStore',
	fields:['role','description'],
	idProperty:'role',
	data:[
		['admin','Администратор системы '],
		['reader','Доступ к просмотру сообщений '],
		['editor','Доступ к настройкам каналов клиентов '],
		['sender','Доступ к рассылке сообщений '],
		['restservice','Доступ к REST сервисам ']
	]
});
