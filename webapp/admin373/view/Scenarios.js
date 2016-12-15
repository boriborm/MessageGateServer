Ext.define('admins.view.Scenarios', { 	
	extend:'Ext.Container',
	requires:['admins.controller.Scenarios'],
	controller: 'ScenariosController',
	cls:'scenarios-panel',
	layout:{type:'vbox', align:'stretch'},
	//defaults:{margin:'5 5 5 5'},
	items:[		
		{	html:'<div class="page-header">Сценарии</div>',
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
					store:'scenariosStore',
					columns:[
						{ text: 'Id',  dataIndex: 'id', width:100},
						{ text: 'Название', dataIndex: 'name', flex: 1},
						{ text: 'Активен',  dataIndex: 'active',width:80, align:'center', renderer:'checkboxRenderer'},
						{ text: 'Сценарий по умолчанию',  dataIndex: 'isDefault',width:80, align:'center', renderer:'checkboxRenderer'},
						{ text: 'Каналы',  dataIndex: 'flow',width:280, renderer:'channelsRenderer'
						}
					],
					tbar: { 
						xtype: 'pagingtoolbar',
						displayInfo: true,
						store:'scenariosStore',
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
					title:'Настройки сценария',
					border:1,
					layout:{type:'vbox', align:'stretch'},
					bodyPadding:'5 5 5 5',
					defaults:{anchor:'100%', labelWidth:150, labelAlign:'right', margin:'5 5 5 5'},
					items:[
						{	xtype:'displayfield',
							name: 'id',
							cls:'data-field',
							fieldLabel:'Id'
						},
						{	xtype:'textfield',
							name: 'name',
							cls:'data-field',
							fieldLabel:'Название'							
						},
						{	xtype:'displayfield',
							name: 'key',
							cls:'data-field',
							fieldLabel:'Ключ'
						},
						{	xtype:'checkboxfield',
							inputValue:1, 
							boxLabel  : 'Активен',
							name:'active',
							margin:'5 5 5 155'
						},												
						{	xtype:'grid',
							reference: 'channelsGrid',
							model:'admins.model.ScenarioFlow',
							sortableColumns:false,
							enableColumnHide:false,
							enableColumnMove:false,
							viewConfig: {
								plugins: {
									ptype: 'gridviewdragdrop'
								},
								listeners:{
									drop:function(node, data, overModel, dropPosition){
										if (data.records.length==1){
											var rec = data.records[0]
											this.refresh();
										}
									},
									beforedrop: function(node, data, overModel, dropPosition, dropHandlers){
										dropHandlers.wait = true;
										Ext.MessageBox.confirm('Подтверждение перемещения', 'Вы уверены?', function(btn){
											if (btn === 'yes') {
												dropHandlers.processDrop(); 									
											} 
											else dropHandlers.cancelDrop();
										});
									}
								}
							},
							columns: [
								{ text: 'Канал',  dataIndex: 'channel', width:100, flex:1},
								{ text: 'Отправитель',  dataIndex: 'from', width:100, flex:1},
								{	xtype:'actioncolumn',
									width:40,
									align:'center',
									items: [{
										icon: '/resources/images/delete.png',
										tooltip: 'Удалить',
										handler: 'btnDeleteChannel'
									}]
								}
							],
							tbar:[
								{	xtype:'button',
									text:'Добавить / обновить канал',
									handler:'btnAddChannel'
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


Ext.define('admins.view.AddScenario', { 	
	extend:'Ext.form.Panel',
	requires:['admins.controller.Scenarios'],
	controller: 'AddScenarioController',
	floating:true,
	title:'Создание нового сценария',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:160,
	itemId:'addScenario',	
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'textfield',
			name: 'name',
			itemId:'name',
			anchor:'90%',
			cls:'data-field',
			fieldLabel:'Название',
			allowBlank:false
		}		
	],
	bbar:[
		{xtype:'tbfill' },
		{xtype:'button', handler:'btnAdd',text:'Создать'},
		{xtype:'button', handler:'btnClose',text:'Закрыть'}
	]
});

Ext.define('admins.view.AddScenarioChannel', { 	
	extend:'Ext.form.Panel',
	requires:['admins.controller.Scenarios'],
	controller: 'AddScenarioChannelController',
	floating:true,
	title:'Добавление канала',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:200,
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'combo',
			store: Ext.create('Ext.data.ArrayStore',{
				fields:[{name:'channel', type:'string'}],
				data:[['SMS'],['VIBER'],['PARSECO'], ['VOICE']]
			}),
			name: 'channel',
			itemId:'channel',
			displayField:'channel',
			valueField:'channel',
			anchor:'50%',
			cls:'data-field',
			fieldLabel:'Канал',
			allowBlank:false,	
			editable:false		
		},
		{	xtype:'textfield',
			name: 'from',
			itemId:'from',
			anchor:'100%',
			cls:'data-field',
			fieldLabel:'Отправитель',
			allowBlank:false
		}
	],
	bbar:[
		{xtype:'tbfill' },
		{xtype:'button', handler:'btnAdd',text:'Добавить'},
		{xtype:'button', handler:'btnClose',text:'Закрыть'}
	]
});

