Ext.define('Product.Window',{
	extend: 'Ext.window.Window'
	,alias: ['widget.productwindow']
	,constrainHeader: true
	,maximizable: true
	,title: '$view.new $Product'
    ,icon: '$g.options.images/Product.png'
	,width: 685
	,height: 473
	,layout: 'fit'
});