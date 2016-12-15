Ext.define('admins.view.Toolbar', { 	
	extend:'Ext.toolbar.Toolbar',
	cls:'admins-toolbar',
	border:0,
	itemId:'toolbar',
	defaults:{cls:'toolbar-button'},
	items:[ 
		{	text:'Общие настройки',
			itemId:'btnToolbarPreferences',
		},
		{	text:'Сценарии',
			itemId:'btnToolbarScenarios'
		},
		{	text:'Типы сообщений',
			itemId:'btnToolbarMessageTypes'
		},
		{	text:'Пользователи',
			itemId:'btnToolbarUsers'
		},
		'->',
		{	text:'АРМ Оператора',
			itemId:'btnToolbarOperator',
			href:'/opers/index.html',
			hrefTarget:'_blank'
		},
	]
});
