/************** КЛАСС Application ********************************/
Ext.define('opers.Application', { 
  extend:'global.classes.BaseApplication',
  requires:[
	'global.classes.AppStarter',
	'global.classes.APReport',
	'DMDS.proxy.DmdsProxy',
	'DMDS.proxy.DmdsWriter',
	'Ext.ux.form.ItemSelector',
	'DMDS.view.DemandPrinter'
  ],
  controllers: ['Controller'],
  name: 'opers',
  appFolder: '/opers',
  appName:'Message Gate Server. АРМ Оператора',
  storesToPreload:new Array(),
  launch: function(){  
	this.callParent();
	//this.controller = this.getDmdsControllerController();
	//this.storesToPreload[0] = this.controller.getFilialsStore();	
					
	/* Создаем глобальный Viewport, если это не печать заявки */

	//AppStarter.start(this);	
	
	console.log('app start');
	
  },
  start:function(){
		var app=this;
		
				
		//var firstpanel = Ext.create('DMDS.view.DemandsGlobalPanel', {});								
		
		//app.globalViewPort.setDataPanel(firstpanel);
		
		//app.getDmdsControllerController().filterDemands();
		
  }
});
