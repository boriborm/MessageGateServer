Ext.define('admins.view.MessageTypes', { 	
	extend:'Ext.Container',
	requires:['admins.controller.MessageTypes'],
	controller: 'MessageTypesController',
	itemId:'messageTypesPanel',
	cls:'messagetypes-panel',
	layout:{type:'vbox', align:'stretch'},	
	items:[ 
		{	html:'<div class="page-header">Типы сообщений</div>',
			height:50
		},
		{
			xtype: 'container',
			flex:1,
			padding:'5 5 5 5',			
			layout: {type:'hbox', align:'stretch'},
			items:[
				{	xtype: 'grid',
					reference: 'gridPanel',
					cls:'grid-panel',
					flex:1,
					border:1,
					store:'messageTypesStore',
					columns:[
						{ text: 'Тип',  dataIndex: 'typeId', width:150},
						{ text: 'Описание', dataIndex: 'description', flex: 1},
						{ text: 'Активен', dataIndex: 'active',width:80, align:'center', renderer:'checkboxRenderer'},
						{ text: 'SMS',  dataIndex: 'acceptSms',width:80, align:'center', renderer:'checkboxRenderer'},
						{ text: 'Viber',  dataIndex: 'acceptViber',width:80, align:'center', renderer:'checkboxRenderer'},
						{ text: 'Voice',  dataIndex: 'acceptVoice',width:80, align:'center', renderer:'checkboxRenderer'},
						{ text: 'Parseco',  dataIndex: 'acceptParseco',width:80, align:'center', renderer:'checkboxRenderer'}
					],
					tbar: { 
						xtype: 'pagingtoolbar',
						displayInfo: true,
						store:'messageTypesStore',
						buttons:[
							{	xtype:'button',
								text:'Добавить',
								handler:'btnAdd'
							},
							{	xtype:'button',
								text:'Удалить',
								handler:'btnDelete'
							}					
						]
					},
					listeners:{
						select:'selectGrid'
					}
				},
				{	xtype:'splitter'},
				{	xtype:'form',
					flex:1,
					cls:'data-panel',
					minWidth:600,
					reference: 'dataPanel',
					collapsible:true,
					collapsed:true,
					collapseDirection:'right',
					title:'Настройки типа сообщения',
					border:1,
					layout:{type:'vbox', align:'stretch'},
					bodyPadding:'5 5 5 5',
					defaults:{anchor:'100%', labelWidth:150, labelAlign:'right', margin:'5 5 5 5'},
					items:[
						{	xtype:'displayfield',
							name: 'typeId',
							itemId:'typeId',
							cls:'data-field',
							fieldLabel:'Тип'
						},
						{	xtype:'textfield',
							name: 'description',
							itemId:'description',
							cls:'data-field',
							fieldLabel:'Описание'							
						},
						{	xtype:'checkboxfield',
							inputValue:1, 
							boxLabel  : 'Активен',
							name:'active',
							itemId:'active',
							margin:'5 5 5 155'
						},
						{	xtype:'fieldcontainer',
							layout:'hbox',
							fieldLabel: 'Доступные каналы',
							//padding:'5 5 5 5',
							defaults:{margin:'0 10 0 10', inputValue:1, xtype:'checkbox'},
							items:[
								{	boxLabel  : 'SMS',
									name:'acceptSms',
									itemId:'acceptSms'
								},
								{	boxLabel  : 'Viber',
									name:'acceptViber',
									itemId:'acceptViber'
								},
								{	boxLabel  : 'Voice',							
									name:'acceptVoice',
									itemId:'acceptVoice'
								},
								{	boxLabel  : 'Parseco',
									name:'acceptParseco',
									itemId:'acceptParseco'
								}
							]														
						},
						{	xtype:'container',
							layout: {type:'vbox'},
							defaults:{labelWidth:270, labelAlign:'right', margin:'5 5 5 5', width:360, minValue:1, maxValue:2048, cls:'data-field', xtype:'numberfield'},
							layout:'vbox',
							items:[
								{	name: 'smsValidityPeriod',
									itemId:'smsValidityPeriod',
									fieldLabel:'Период доставки через канал SMS, сек.'									
								},
								{	name: 'viberValidityPeriod',
									itemId:'viberValidityPeriod',
									fieldLabel:'Период доставки через канал Viber, сек.'
								},
								{	name: 'voiceValidityPeriod',
									itemId:'voiceValidityPeriod',
									fieldLabel:'Период доставки через канал Voice, сек.'
								},
								{	name: 'parsecoValidityPeriod',
									itemId:'parsecoValidityPeriod',
									fieldLabel:'Период доставки через канал Parseco, сек.'
								}
							]
						},
						{	xtype:'container',
							layout: {type:'hbox', align:'stretch', pack:'center'},
							padding:' 5 5 5 5',
							items:[
								{	xtype:'button',
									text:'Сохранить',
									handler:'btnSave'
								}
							]
						}
					]
				}
			]
		}
	
	]
});


Ext.define('admins.view.AddMessageType', { 	
	extend:'Ext.form.Panel',
	requires:['admins.controller.MessageTypes'],
	controller: 'AddMessageTypeController',
	floating:true,
	title:'Добавление нового типа сообщения',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:200,
	itemId:'addMessageTypePanel',	
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'textfield',
			name: 'typeId',
			itemId:'typeId',
			anchor:'50%',
			cls:'data-field',
			fieldLabel:'Тип'
		},
		{	xtype:'textfield',
			name: 'description',
			itemId:'description',
			anchor:'100%',
			cls:'data-field',
			fieldLabel:'Описание'
		}
	],
	bbar:[
		{xtype:'tbfill' },
		{xtype:'button', handler:'btnAdd',text:'Добавить'},
		{xtype:'button', handler:'btnClose',text:'Закрыть'}
	]
});


