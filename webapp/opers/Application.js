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
		{button:"btnToolbarMessages", roles:["reader"]},
		{button:"btnToolbarPhoneGrants", roles:["editor"]},
		{button:"btnToolbarSendOneToAll", roles:["sender"]}
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
	}
});
