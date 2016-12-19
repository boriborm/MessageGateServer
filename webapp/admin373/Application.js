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
	accessForRoles:['admin'],
	toolbarGrants:[
		{button:"btnToolbarScenarios", roles:["admin"]},
		{button:"btnToolbarMessageTypes", roles:["admin"]},
		{button:"btnToolbarUsers", roles:["admin"]}
	],
	launch: function(){ 
		var app = this;
		app.controller = this.getController('admins.controller.Controller');
		// fill storesToPreload array;
		//app.storesToPreload[0] = Ext.data.StoreManager.lookup('scenariosStore');
		//app.storesToPreload[1] = Ext.data.StoreManager.lookup('messageTypesStore');		
		
		this.callParent(arguments);
	
	},
	checkPermissions: function(){
		console.log('perms');
		this.loadStoresAndRun();
	},
	start:function(){

		var app=this;
		var toolbar = Ext.create('admins.view.Toolbar',{});		
				
		app.addToolbar(toolbar);		
		app.controller.app = this;
		if (app.user.hasRoles(["admin"])){
			
			//app.controller.messageTypesPanel = app.controller.getMessageTypesView().create();
			//app.controller.scenariosPanel = app.controller.getScenariosView().create();		
			//app.controller.usersPanel = app.controller.getUsersView().create();
		}
	}
});
