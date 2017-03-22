Ext.define('global.classes.BaseApplication', { 
	extend:'Ext.app.Application',
	requires:[
		'Ext.container.Viewport',
		'global.classes.GlobalViewPort', 
	],
	launch: function(){

		var me = this;		
		me.globalViewPort = Ext.create('global.classes.GlobalViewPort',{});		
		me.dataPanel = Ext.getCmp('globaldatapanel');
		
		Ext.Ajax.request({
			url: '/rest/session/getuser',
			success: function(response){				
				me.globalViewPort.getEl().unmask();
				var jResponse = Ext.decode(response.responseText);
				if (jResponse.success){
					
					me.globalViewPort.getEl().unmask();
					me.user = jResponse.data;
					Ext.Ajax.setDefaultHeaders({'User-Token':me.user.userToken});
					me.initApp();
				} else {
					window.location="/authorize.html?back="+encodeURIComponent(window.location);
				}
			},
			failure: function(){
				me.globalViewPort.getEl().unmask();
			}
		});		
		
		Ext.QuickTips.init();
	
		var stateProv;
		if (Ext.supports.LocalStorage) stateProv = new Ext.create('Ext.state.LocalStorageProvider');
		else stateProv = new Ext.create('Ext.state.CookieProvider')	
		Ext.state.Manager.setProvider(stateProv);	
		
		
//		GlobalToolbar.initMenu();
		
		
				
	},
	initApp:function(){
				
		var me = this;
		
		me.globalViewPort.getViewModel().set('appName',me.appName);
		me.globalViewPort.getViewModel().set('userName',me.user.userName);
		
		me.user.hasRoles = function (roleNames){			
			var intersect = Ext.Array.intersect(roleNames, me.user.roles);
			return (intersect.length>0);
		}
		
		me.checkPermissions();		
	},
	checkPermissions: function(){
		var me = this;
		var roleArray = me.accessForRoles||[];
		if (roleArray.length>0){
			if (me.user.hasRoles(roleArray)){
				me.loadStoresAndRun();
			} else Ext.Msg.alert('Ошибка доступа', 'Доступ запрещён');
		}else me.loadStoresAndRun();
	},	
	loadStoresAndRun:function(){		
		var loadMask;
		var me = this;
		
		//Определяем функцию проверки загруженности сторов.
		// по умолчанию берём тут, но если задано в Application, то берём оттуда
		var verifyStoreLoads = function(){		
			var ret = true;
			if (me.verifyStoreLoads) 
				ret = me.verifyStoreLoads();
			else{
				//Если массив сторов не задавали, то возвращаем true
				if (!me.storesToPreload) return true;
				//проверяем массив сторов на факт загрузки данных
				for (i=0;i<me.storesToPreload.length;i++){
					ret=ret&&(me.storesToPreload[i]).isLoadedSuccessful;
					if (!ret){
						break;
					}	
				}	
			}
			return ret;
		};
		var preRun = function(){	
			if (!loadMask){
				loadMask = new Ext.LoadMask(me.globalViewPort, {msg:'Загрузка справочников...'});
				loadMask.show();
			}
			
			var ret = verifyStoreLoads();
			if (ret){

				loadMask.hide()
				loadMask.destroy();
				me.start();
			}
			else
				setTimeout(preRun,1000);		
		};
		
		//Если в application задана своя функция загрузки сторов, то используем её
		// если не задана, то пытаемся загрузить из массива в application storesToPreload.
		if (me.loadStores) {
			me.loadStores();
		}else{
			if (me.storesToPreload){
				for (i=0;i<me.storesToPreload.length;i++){
					me.storesToPreload[i].load();
				}
			}
		}
		
		preRun();
	},

    addAbout:function(aboutWindow){
		if (!aboutWindow) return;
		if(this.rightToolBar){
			var aboutButton = Ext.create('Ext.button.Button',{
				text:'О программе',
				handler: function(){
					var about = Ext.getCmp('aboutWindow');
					if(!about) about = aboutWindow;
					about.show();
				}
			});
			this.rightToolBar.add(aboutButton);
		}
	},
    setDataPanel:function(panel){
		if (this.dataPanel.currentPanel) this.dataPanel.currentPanel.destroy();		
		this.dataPanel.removeAll(false);
		this.dataPanel.add(panel);
		this.dataPanel.currentPanel = panel;
		this.setLockPanel(this.globalViewPort);
	},
	setLockPanel:function(panel){
	   var me = this;
		if (me.blocksession){
			var createTimer = function(){
				me.activeTimerId = setTimeout(
					function(){
						/* Редирект на авторизацию */
					},
					me.blocksessionseconds * 1000
				);
			}
			createTimer();			
			panel.getEl().on('mousemove', function(e){
				if (me.activeTimerId!=0) 
					clearTimeout(me.activeTimerId);
				createTimer();
			});
			panel.getEl().on('keypress', function(e){
				if (me.activeTimerId!=0) 
					clearTimeout(me.activeTimerId);
				createTimer();
			});
		}
	},
	addToolbar: function(toolbar){
		var me = this,
			buttons, button, hasRole;
		var grants = me.toolbarGrants||[];
		Ext.Array.forEach(grants, function(grant, index){
			buttons = toolbar.query("#"+grant.button);
			if (buttons&&buttons.length==1){
				button = buttons[0];
				
				var intersectArray = Ext.Array.intersect(grant.roles, me.user.roles);				
				(intersectArray.length>0?button.enable():button.disable());
			}			
		});
		
		
		var tbContainer = Ext.getCmp('globaltoolbar');
		tbContainer.add(toolbar);
	}
}); 
