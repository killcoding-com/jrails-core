Ext.define('Product.Form',{
	extend: 'Ext.form.Panel'
	,alias: ['widget.productform']
	,border: 0
	,autoScroll: true
	,buttonAlign: 'align'	
	,bodyPadding: '10 5 5 5'
	,buttonAlign: 'center'
	,removeButton: false
	,defaults:{
		labelWidth: 70
		,padding: '5 10 5 5'
	}
	,layout: { 
			type: 'column'
		}
    ,initComponent: function(){
        var me = this;
        Ext.apply(me,{
            	items:[
            	       {
            	   		 xtype: 'hiddenfield'
            	   		 ,name: 'Product[id]'
            	   	   }
                	   ,{
            		     xtype: 'textfield'
            		     ,name: 'Product[code]'
            		     ,fieldLabel: '$Product.code'
            	       }
                	   ,{
            		     xtype: 'textfield'
            		     ,name: 'Product[name]'
            		     ,fieldLabel: '$Product.name'
            	       }
            	]
        });
        me.callParent(arguments);
    }
});
