Ext.define('admins.view.Processors', { 	
	extend:'Ext.Container',
	requires:['admins.controller.Processors'],
	controller: 'ProcessorsController',
	cls:'processors-panel',
	layout:{type:'vbox', align:'stretch'},
	viewModel:{
		data:{
			queueIsActive:false,
			fileIsActive:false,
			deliveryIsActive:false
		}
	},
	items:[ 
		{	html:'<div class="page-header">Процессы и настройки</div>',
			height:50
		},
		{	xtype: 'panel',
			flex:0,
			border:0,
			height:50,
			bodyPadding:'5 5 5 5',
			items:[
				{ 	xtype:'button',
					text:'Обновить статусы',
					handler:'getStatus'
				}
			]
		},
		{
			xtype: 'container',
			flex:1,
			padding:'5 5 5 5',			
			layout: {type:'vbox', align:'stretch'},
			items:[			
				{	xtype:'panel',
					layout:{type:'hbox', aligh:'stretch'},
					border:1,
					minHeight:50,
					defaults:{padding:'5 5 5 5'},
					items:[
						{	xtype:'container',
							html:'Процесс обработки очереди сообщений',
							width:200
						},
						{	xtype:'container',
							reference:'queueStatus',
							flex:1
						},
						{	xtype:'container',
							width: 210,
							defaults:{margin:'5 5 5 5', width:90, xtype:'button'},
							items:[
								{	text:'Запустить',									
									reference:'startQueue',
									handler:'startQueue',
									bind:{
										disabled:'{queueIsActive}'
									}
								},
								{	text:'Остановить',
									reference:'stopQueue',
									handler:'stopQueue',
									bind:{
										disabled:'{!queueIsActive}'
									}
								}
							]
						}					
					]
				},
				{	xtype:'panel',
					layout:{type:'hbox'},
					border:1,
					minHeight:50,
					defaults:{padding:'5 5 5 5'},
					items:[
						{	xtype:'container',
							html:'Процесс обработки файлов',
							width:200
						},
						{	xtype:'container',
							html:'',
							reference:'fileStatus',
							flex:1
						},
						{	xtype:'container',
							width: 210,
							defaults:{margin:'5 5 5 5', width:90, xtype:'button'},
							items:[
								{	text:'Запустить',
									reference:'startFile',
									handler:'startFile',
									bind:{
										disabled:'{fileIsActive}'
									}
								},
								{	text:'Остановить',
									reference:'stopFile',
									handler:'stopFile',
									bind:{
										disabled:'{!fileIsActive}'
									}
								}
							]
						}					
					]
				},
				{	xtype:'panel',
					layout:{type:'hbox'},
					border:1,
					minHeight:50,
					defaults:{padding:'5 5 5 5'},
					items:[
						{	xtype:'container',
							html:'Процесс обработки отчётов о доставке',
							width:200
						},
						{	xtype:'container',
							html:'',
							reference:'deliveryStatus',
							flex:1
						},
						{	xtype:'container',
							width: 210,
							defaults:{margin:'5 5 5 5', width:90, xtype:'button'},
							items:[
								{	text:'Запустить',
									reference:'startDelivery',
									handler:'startDelivery',
									bind:{
										disabled:'{deliveryIsActive}'
									}
								},
								{	text:'Остановить',
									reference:'stopDelivery',
									handler:'stopDelivery',
									bind:{
										disabled:'{!deliveryIsActive}'
									}
								}
							]
						}					
					]
				},
				{	xtype:'panel',
					flex:1,
					border:0
				}
			]
		}
	
	]
});


