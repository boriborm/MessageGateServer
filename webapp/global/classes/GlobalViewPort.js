Ext.define('global.classes.GlobalViewPort', {
	alternateClassName: 'GlobalViewPort',
    extend: 'Ext.container.Viewport',
    layout: 'border',
    viewModel:{
		data:{
			appName: null,
			userName: null
		}
	},
    items:[
	{
		region: 'north', 
        xtype: 'container',  
		border:0,
		layout:'anchor',
		items:[
			{	xtype: 'container',
				id:'globalheader',
				height:35,
				layout: {type:'hbox', align:'stretch'},
				defaults:{xtype:'container', padding:'5 10 5 10'},
				items:[
					{
						id: 'globalFieldApplicationName',
						flex:1,
						bind:{
							html:'<div class="app-name">{appName}</div>',
						}
					},
					{
						id: 'globalFieldUserName',
						flex:1,
						bind:{
							html:'<div class="user-name">{userName}</div>',
						}
					}
				]
			},
			{		
			 cls:'globaltoolbar',
			 xtype:'panel',
			 border:1,
			 anchor:'100%',
			 layout:{type:'hbox', align:'stretch'},			 
			 items:[
				 {	xtype: 'container',
					id:'globaltoolbar',
					flex:1,	
				 },
				 {	xtype:'toolbar',
					id:'globalrighttoolbar',
					items:[
						{	text: 'Выход',
							handler:function(){						
								Ext.Ajax.request({
									url: '/rest/session/logout',
									success:function(){location.href="/"},
									failure: function(){location.href="/"}
								});
							}
						}
					]
				}
			 ]
			}
		]
	},
	{	
		region: 'center', 
        xtype: 'container',
		layout:'fit',
		id:'globaldatapanel',
		bodyPadding: 3, 
    	border:true,
    	bodyStyle:{background:'none'}
	}
   ]
});
