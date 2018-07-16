Ext.define('Product.ComboBox',{
	extend: 'Ext.form.field.ComboBox'
	,alias: ['widget.productcombobox']
	,valueField: 'as_product_id'
	,displayField: 'as_product_id'
	,fieldLabel: '$Product'
	,emptyText: ''
	,pageSize: 10
	,minChars: 1
	,triggerAction: 'all'
	,forceSelection: true
	,selectOnFocus: false
	,autoScroll: true
	,anchor: '100%'
    ,initComponent: function(){
		var me = this;
		me.callParent(arguments);
		me.store = new Ext.data.Store({
    		 fields: ['as_product_id','as_product_id']
	 		,proxy: {
		 			type: 'ajax'
		 			,url: '$g.options.domainUrl/Product/boxList/admin'
		 			,reader:{
		 				type: 'json'
		 				,idProperty: 'as_product_id'
		 				,root: 'data'
		 				,totalProperty: 'total'
		 			}
		 			,extraParams: {
		 				and: {eq_deleted:false}
		 			}
		 	 }
    	 });
	}
});
