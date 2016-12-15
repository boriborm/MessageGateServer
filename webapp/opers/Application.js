/************** КЛАСС Application ********************************/
Ext.define('opers.Application', { 
	extend:'global.classes.BaseApplication',
	requires:[
		'opers.view.Toolbar'
	],
	name: 'opers',
	appFolder: '/opers',
	appName:'Message Gate Server. АРМ Оператора',
	storesToPreload:new Array(),  
	controllers:['Controller'],
	toolbarGrants:[
		{button:"btnToolbarPhoneGrants", roles:["admin","editor"]},
		{button:"btnToolbarSendOneToAll", roles:["admin","sender"]}		
	],
	launch: function(){ 
		var app = this;
		app.controller = this.getController('opers.controller.Controller');
		// fill storesToPreload array;
		app.storesToPreload[0] = Ext.data.StoreManager.lookup('scenariosStore');
		app.storesToPreload[1] = Ext.data.StoreManager.lookup('messageTypesStore');		
		
		this.callParent(arguments);
	
	},
	start:function(){

		var app=this;
		var toolbar = Ext.create('opers.view.Toolbar',{});		
				
		app.addToolbar(toolbar);		
		app.controller.app = this;
		
		if (app.user.hasRoles(["admin","reader"])){
			app.controller.messagesPanel = app.controller.getMessagesView().create();
			app.controller.messagesPanel.filterPanel = app.controller.messagesPanel.query('#filterPanel')[0];
			app.controller.messagesPanel.gridPanel = app.controller.messagesPanel.query('#gridPanel')[0];
			app.controller.messagesPanel.dataPanel = app.controller.messagesPanel.query('#dataPanel')[0];
			app.controller.messagesPanel.dataPanel.reportsGrid = app.controller.messagesPanel.dataPanel.query('#reportsGridPanel')[0];
		}
		if (app.user.hasRoles(["admin","sender"])){
			app.controller.sendOneToAllPanel = app.controller.getSendOneToAllView().create();
			app.controller.sendOneToAllPanel.dataPanel = app.controller.sendOneToAllPanel.query('#dataPanel')[0];
			app.controller.sendOneToAllPanel.dataPanel.gridPhones = app.controller.sendOneToAllPanel.query('#gridPhones')[0];
		}
		if (app.user.hasRoles(["admin","editor"])){	
			app.controller.phoneGrantsPanel = app.controller.getPhoneGrantsView().create();
			app.controller.phoneGrantsPanel.dataPanel = app.controller.phoneGrantsPanel.query('#dataPanel')[0];
		}
	}
});
