/************** КЛАСС Application ********************************/
Ext.define('admins.Application', { 
	extend:'global.classes.BaseApplication',
	requires:[
		'admins.view.Toolbar'
	],
	name: 'admins',
	appFolder: msgAdminsPath,
	appName:'Message Gate Server. АРМ Администратора',
	storesToPreload:new Array(),  
	controllers:['Controller'],
	toolbarGrants:[
		{button:"btnToolbarPhoneGrants", roles:["admin"]},
		{button:"btnToolbarSendOneToAll", roles:["admin"]}
	],
	launch: function(){ 
		var app = this;
		app.controller = this.getController('admins.controller.Controller');
		// fill storesToPreload array;
		//app.storesToPreload[0] = Ext.data.StoreManager.lookup('scenariosStore');
		//app.storesToPreload[1] = Ext.data.StoreManager.lookup('messageTypesStore');		
		
		this.callParent(arguments);
	
	},
	start:function(){

		var app=this;
		var toolbar = Ext.create('admins.view.Toolbar',{});		
				
		app.addToolbar(toolbar);		
		app.controller.app = this;
		if (app.user.hasRoles(["admin"])){
			console.log('cr');
			app.controller.preferencesPanel = app.controller.getPreferencesView().create();
			app.controller.preferencesPanel.filterPanel = app.controller.preferencesPanel.query('#filterPanel')[0];
			app.controller.preferencesPanel.gridPanel = app.controller.preferencesPanel.query('#gridPanel')[0];
			
			app.controller.messageTypesPanel = app.controller.getMessageTypesView().create();
			//app.controller.messageTypesPanel.filterPanel = app.controller.messageTypesPanel.query('#filterPanel')[0];
			//app.controller.messageTypesPanel.gridPanel = app.controller.messageTypesPanel.query('#gridPanel')[0];
			//app.controller.messageTypesPanel.dataPanel = app.controller.messageTypesPanel.query('#dataPanel')[0];
			
			app.controller.scenariosPanel = app.controller.getScenariosView().create();
			
			app.controller.usersPanel = app.controller.getUsersView().create();
			//app.controller.scenariosPanel.filterPanel = app.controller.scenariosPanel.query('#filterPanel')[0];
			//app.controller.scenariosPanel.gridPanel = app.controller.scenariosPanel.query('#gridPanel')[0];
			//app.controller.scenariosPanel.dataPanel = app.controller.scenariosPanel.query('#dataPanel')[0];
			//app.controller.scenariosPanel.dataPanel.channelsGrid = app.controller.scenariosPanel.dataPanel.query('#channelsGrid')[0];			
		}
		/*
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
		*/ 
	}
});
