Ext.define('Product.Tab',{
	extend: 'Ext.panel.Panel'
	,alias: ['widget.producttab']
	,requires: [
	            'Product.Search'
	            ,'Product.Grid'
	          ]
	,title: '$Product'
	,autoScroll: false
	,closable: true
	,layout: 'border'
    ,icon: '$g.options.images/Product.png'
	,items:[
	       {
	    	   xtype: 'productsearch'
	    	   ,region: 'north'
	    	   ,split: true
	       }
	       ,{
	    	   xtype: 'productgrid'
	    	   ,region: 'center'
	       }
      ]

});