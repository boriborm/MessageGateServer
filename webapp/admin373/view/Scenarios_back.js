Ext.define('admins.view.Scenarios', { 	
	extend:'Ext.Container',
	itemId:'scenariosPanel',
	cls:'scenarios-panel',
	layout:{type:'vbox', align:'stretch'},
	defaults:{padding:'5 5 5 5'},
	items:[	
		{
			xtype: 'container',
			flex:1,
			layout: {type:'hbox', align:'stretch'},
			items:[
				{	xtype: 'grid',
					itemId: 'gridPanel',
					cls:'grid-panel',
					flex:1,
					border:1,
					store:'scenariosStore',
					columns:[
						{ text: 'Id',  dataIndex: 'id', width:100},
						{ text: 'Название', dataIndex: 'name', flex: 1},
						{ text: 'Активен',  dataIndex: 'active',width:80, align:'center', renderer:function(value){ if (value) return 'V'; else return 'X';}},
						{ text: 'Сценарий по умолчанию',  dataIndex: 'isDefault',width:80, align:'center', renderer:function(value){ if (value) return 'V'; else return 'X';}},
						{ text: 'Каналы',  dataIndex: 'flow',width:280, 
							renderer:function(channels){
								var output = '';
								Ext.Array.each(channels, function(item){
									console.log(item.channel);
									output = output + item.channel+" -> ";
								});
								if (output) return output.substr(0,output.length-4);
								return '';
							}
						}
					],
					tbar:[
						{	xtype:'button',
							text:'Добавить',
							itemId:'btnAddScenario'
						},
						{	xtype:'button',
							text:'Удалить',
							itemId:'btnDeleteScenario'
						}					
					],
					bbar: { 
						xtype: 'pagingtoolbar',
						displayInfo: true,
						store:'scenariosStore'
					}
				},
				{	xtype:'splitter'},
				{	xtype:'form',
					flex:1,
					cls:'data-panel',
					minWidth:600,
					itemId: 'dataPanel',
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
							itemId:'id',
							cls:'data-field',
							fieldLabel:'Id'
						},
						{	xtype:'textfield',
							name: 'name',
							itemId:'name',
							cls:'data-field',
							fieldLabel:'Название'							
						},
						{	xtype:'displayfield',
							name: 'key',
							itemId:'key',
							cls:'data-field',
							fieldLabel:'Ключ'
						},
						{	xtype:'checkboxfield',
							inputValue:1, 
							boxLabel  : 'Активен',
							name:'active',
							itemId:'active',
							margin:'5 5 5 155'
						},
						
						{	xtype:'checkboxfield',
							inputValue:1, 
							boxLabel  : 'Сценарий по умолчанию',
							name:'isDefault',
							itemId:'isDefault',
							margin:'5 5 5 155'
						},
						{	xtype:'grid',
							itemId: 'channelsGrid',
							model:'admins.model.ScenarioFlow',
							sortableColumns:false,
							enableColumnHide:false,
							enableColumnMove:false,
							forceFit:true,
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
								{ text: 'Канал',  dataIndex: 'channel', width:100},
								{ text: 'Отправитель',  dataIndex: 'from', width:100},
								{	xtype:'actioncolumn',
									width:30,
									align:'center',
									items: [{
										icon: '/resources/images/delete.png',
										tooltip: 'Удалить',
										itemId:'btnDeleteChannel',
										handler: function(grid, rowIndex, colIndex) {
											var rec = grid.getStore().getAt(rowIndex);											
											Ext.MessageBox.confirm('Подтверждение удаления', 'Вы уверены, что хотите удалить канал '+rec.data.channel+' ?', function(btn){
												if (btn === 'yes') {
													grid.getStore().removeAt(rowIndex);
												}
											});
										}
									}]
								}
							],
							tbar:[
								{	xtype:'button',
									text:'Добавить / обновить канал',
									itemId:'btnAddScenarioChannel'
								}
							]
						},
						{	xtype:'container',
							layout: {type:'hbox', align:'stretch', pack:'center'},
							padding:' 5 5 5 5',
							items:[
								{	xtype:'button',
									text:'Сохранить',
									itemId:'btnScenarioSave'
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
	floating:true,
	title:'Создание нового сценария',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:200,
	itemId:'addScenario',	
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'textfield',
			name: 'name',
			itemId:'name',
			anchor:'50%',
			cls:'data-field',
			fieldLabel:'Название',
			allowBlank:false
		}		
	],
	bbar:[
		{xtype: 'tbfill' },
		{xtype:'button', itemId:'btnAdd',text:'Создать'},
		{xtype:'button', itemId:'btnClose',text:'Закрыть'}
	]
});

Ext.define('admins.view.AddScenarioChannel', { 	
	extend:'Ext.form.Panel',
	floating:true,
	title:'Добавление канала',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:200,
	itemId:'addScenarioChannel',	
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
		{ xtype: 'tbfill' },
		{xtype:'button', itemId:'btnAdd',text:'Добавить'},
		{xtype:'button', itemId:'btnClose',text:'Закрыть'}
	]
});

