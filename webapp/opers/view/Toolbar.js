Ext.define('opers.view.Toolbar', { 	
	extend:'Ext.toolbar.Toolbar',
	cls:'opers-toolbar',
	border:0,
	itemId:'toolbar',
	defaults:{cls:'toolbar-button'},
	items:[ 
		{	text:'Отчеты',
			itemId:'btnToolbarMessages',
		},
		{	text:'Отправка сообщения',
			itemId:'btnToolbarSendOneToAll'
		},
		{	text:'Доступ к каналам',
			itemId:'btnToolbarPhoneGrants'
		}		
	]
});
