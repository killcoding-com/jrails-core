Ext.define('Product.Search',{
	extend: 'Ext.form.Panel'
	,alias: ['widget.productsearch']
	,border: 0
	,autoScroll: true
	,buttonAlign: 'align'
	,bodyPadding: '10 5 5 5'
	,buttonAlign: 'center'
	,defaults:{
		labelWidth: 70,
		padding: '5 10 5 5'
	}
	,layout: { 
			type: 'column'
	}
	,items:[
	   {
	     xtype: 'textfield'
		 ,name: 'and[any_code]'
	     ,fieldLabel: '$Product.code'
	   }
       ,
	   {
	     xtype: 'textfield'
		 ,name: 'and[any_name]'
	     ,fieldLabel: '$Product.name'
	   }
	]
	,buttons:[{
		text: '$view.search'
		,icon: '$g.options.images/search.png'
		,fixed: true
		,handler: function(btn,e){
			var task = new Ext.util.DelayedTask(function(){
			    var params = {};
	        	var form = btn.up('form').getForm();
	        	var store = Ext.data.StoreManager.lookup('Product-Store');
	        	Ext.apply(params,store.proxy.extraParams);
	        	Ext.apply(params,form.getValues() || {});
	        	store.currentPage = 1;
	        	store.proxy.extraParams = params;			
	        	store.reload();
	        	Locale.searchSuccess();	        	
				btn.enable();
				btn.setText('$view.search');
			});	
			new Ext.util.DelayedTask(function(){
				btn.disable();
				btn.setText('$view.searching');				
	        	task.delay(200);
			}).delay(1);
		}
	}]

});

