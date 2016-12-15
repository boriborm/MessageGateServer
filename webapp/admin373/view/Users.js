Ext.define('admins.view.Users', { 	
	extend:'Ext.Container',
	requires:['admins.controller.Users'],
	controller: 'UsersController',
	cls:'users-panel',
	layout:{type:'vbox', align:'stretch'},
	items:[	
		{	html:'<div class="page-header">Пользователи</div>',
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
					store:'usersStore',
					columns:[
						{ text: 'id',  dataIndex: 'id', width:100},
						{ text: 'Логин',  dataIndex: 'login', width:100},
						{ text: 'Наименование пользователя', dataIndex: 'userName', flex: 1},
						{ text: 'Блокирован',  dataIndex: 'locked',width:80, align:'center', renderer:'checkboxRenderer'}
					],
					tbar: { 
						xtype: 'pagingtoolbar',
						displayInfo: true,
						store:'usersStore',
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
					title:'Данные пользователя',
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
						{	xtype:'displayfield',
							name: 'login',
							cls:'data-field',
							fieldLabel:'Логин'
						},
						{	xtype:'textfield',
							name: 'userName',
							cls:'data-field',
							fieldLabel:'Наименование пользователя'
						},
						{	xtype:'checkboxfield',
							inputValue:1, 
							boxLabel  : 'Блокирован',
							name:'locked',
							margin:'5 5 5 155'
						},																		
						{	xtype:'grid',
							reference: 'rolesGrid',
							sortableColumns:false,
							enableColumnHide:false,
							enableColumnMove:false,
							store:  Ext.create('Ext.data.ArrayStore',{
								fields:['role'],
								data:[]
							}),
							columns: [
								{ text: 'Роль',  dataIndex: 'role', renderer:'roleRenderer', flex:1},
								{	xtype:'actioncolumn',
									width:40,
									align:'center',
									items: [{
										icon: '/resources/images/delete.png',
										tooltip: 'Удалить',
										handler: 'btnDeleteRole'
									}]
								}
							],
							tbar:[
								{	xtype:'button',
									text:'Добавить роль',
									handler:'btnAddRole'
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


Ext.define('admins.view.AddUser', { 	
	extend:'Ext.form.Panel',
	requires:['admins.controller.Users'],
	controller: 'AddUserController',
	floating:true,
	title:'Создание пользователя',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:190,
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'textfield',
			name: 'userName',
			cls:'data-field',
			fieldLabel:'Наименование пользователя',
			allowBlank:false,
			anchor:'90%'
		},
		{	xtype:'textfield',
			name: 'login',
			cls:'data-field',
			fieldLabel:'Логин',
			allowBlank:false,
			anchor:'50%'
		},
		{	xtype:'textfield',
			name: 'password',
			cls:'data-field',
			fieldLabel:'Пароль',
			allowBlank:false,
			anchor:'50%',
			inputType: 'password'
		}		
	],
	bbar:[
		{xtype:'tbfill' },
		{xtype:'button', handler:'btnAdd',text:'Создать'},
		{xtype:'button', handler:'btnClose',text:'Закрыть'}
	]
});

Ext.define('admins.view.AddUserRole', { 	
	extend:'Ext.form.Panel',
	requires:['admins.controller.Users'],
	controller: 'AddUserRoleController',
	floating:true,
	title:'Добавление роли',
	width:550,
	draggable:true,
	modal:true,
	border:1,
	height:200,
	bodyPadding:'15 15 15 15',
	layout:'anchor',
	defaults:{labelWidth:100, labelAlign:'right'},
	items:[
		{	xtype:'combobox',
			store: 'userRolesStore',
			name: 'role',
			displayField:'description',
			valueField:'role',
			anchor:'90%',
			cls:'data-field',
			fieldLabel:'Роль',
			allowBlank:false,	
			editable:false		
		}
	],
	bbar:[
		{xtype:'tbfill' },
		{xtype:'button', handler:'btnAdd',text:'Добавить'},
		{xtype:'button', handler:'btnClose',text:'Закрыть'}
	]
});

