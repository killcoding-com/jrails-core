Ext.define('Product.Grid',{
	extend: 'Ext.grid.Panel'
	,alias: ['widget.productgrid']
	,requires: [
	            'Product.New'
	            ,'Product.Edit'
 				,'Product.Window'
	          ]
	,forceFit: true
	,store: {
		storeId:'Product-Store'
		,fields: [
		          'as_product_id'
		          ,'as_product_code'
		          ,'as_product_name'
		 ]
		,proxy: {
			type: 'ajax'
			,url: '$g.options.domainUrl/Product/list/admin'
			,reader: {
				type: 'json'
				,root: 'data'
				,totalProperty: 'total' 
			}
			,extraParams: {
				extraAnd: "{eq_deleted:false}" 
				,extraOr: "{}"
			}
		}
		,autoLoad: false
		,remoteSort: true
		,sorters: [{property: 'as_product_created_at', direction: 'DESC'}]		
	}
	,columns:[
        {xtype: 'rownumberer',text:'NO.',width:40,align:'center'}
		,{
			dataIndex: 'as_product_id'
			,hidden: true
			,hideable:false
		}
	   ,{
		    text: '$Product.code'
	        ,dataIndex: 'as_product_code'
	        ,editor: {
		            xtype: 'textfield'
	        }
		}
	   ,{
		    text: '$Product.name'
	        ,dataIndex: 'as_product_name'
	        ,editor: {
		            xtype: 'textfield'
	        }
		}

		
		,{
      	  text: '$view.edit'
          ,xtype:'actioncolumn'
          ,width:46
          ,lockable:true
          ,menuDisabled: true
          ,hideable:false
          ,align:'center'
          ,items: [{
              icon: '$g.options.images/edit.png'
              ,tooltip: '$view.edit'
              ,handler: function(grid, rowIndex, colIndex) {
                  	var row = grid.getStore().getAt(rowIndex);
                  	var id = row.get('as_product_id');
                  	grid.select(rowIndex);
                  	var ar = new ActiveRecord('Product',id);
                  	var winId = 'productwindow-' + ar.data.id;
                  	if(ar.data !== null){
                  		var edit = Ext.getCmp(winId);
                  		if(!edit){
	                  		edit = new Product.Window({
	                  			id: winId
	                  			,items: [{
		                  					xtype: 'productedit'
		                  					,postman: {ar: ar}
	                  					}]
	                  		});
                  		}
                  		edit.setTitle('$view.edit ' + ar.data.id);
                  		ar.write(edit);
                  		edit.show();
                    }else{
                    	Locale.failure(rs.status,rs.msg);
                    }
              }
          }
        ]
      }
	  ,{
      	  text: '$view.remove'
          ,xtype:'actioncolumn'
          ,align:'center'
          ,width:46
          ,lockable:true
          ,menuDisabled: true
          ,hideable:false
          ,items: [{
        	  icon: '$g.options.images/remove.png'
              ,tooltip: '$view.remove'
              ,handler: function(view, rowIndex,colIndex,item,e,record) {
	            	  Locale.rmConfirm(function(){
	            	 		var ar = new ActiveRecord('Product');
	    					var task = new Ext.util.DelayedTask(function(){
	    						var rs = ar.remove();
	        					if(rs.status == 1){
	        						Locale.success();
	        					}else{
	        						Locale.failure(rs.status,rs.msg);
	        					}	
	    						var store = Ext.data.StoreManager.lookup('Product-Store');
	    						store.reload();
	    					});	
	    					new Ext.util.DelayedTask(function(){
	    	                  	var row = view.getStore().getAt(rowIndex);
	    	                  	var id = row.get("as_product_id");
	    	                  	view.select(rowIndex);	    	                  	
	    	                  	ar.data.id = id;	    	                  	
	        					task.delay(200);
	    					}).delay(1);                  					               					
	    				});

              }
          }
        ]
      }
	]
    ,plugins: [
          Ext.create('Ext.grid.plugin.CellEditing', {
              clicksToEdit: 2
              ,listeners: {
          		edit: function(editor,e){
        	  		var id = e.record.get('as_product_id');
          			var attr = e.field.replace(/^as_product_/,'');
          			var nv,ov;
          			if(Ext.isDate(e.value)){
          				nv = Ext.Date.format(e.value,e.column.format);          				
          			}else{
          				nv = e.value || '';
          			}
          			if(Ext.isDate(e.originalValue)){
          				ov = Ext.Date.format(e.originalValue,e.column.format)
          			}else{
          				ov = e.originalValue || '';
          			}
          			if(nv != ov){
          				var ar = new ActiveRecord('Product');
          				ar.data.id = id;
          				ar.data[attr] = nv;
          				var rs = ar.save();
          				if(rs.status == 1){
          					Locale.success();
          				}else{
          					Locale.failure(rs.status,rs.msg);
          				}
          			}
          		}
          	}
          })
	]	
	,initComponent: function() { 
		this.selModel = new Ext.selection.CellModel({});
		this.callParent(arguments);
		var paging = Ext.create('Ext.toolbar.Paging',{
    		pageSize: 25
    		,dock: 'bottom'
    		,displayInfo: true
    		,store: this.getStore()
    	});
		paging.remove(11);
		paging.add(11,'-');
		paging.add(0,[{
			text: '$view.new'
			,icon: '$g.options.images/new.png'
			,handler: function(me,e){
				new Product.Window({
					items: [{xtype: 'productnew'}]
				}).show();
			}
		},'-',{xtype: 'tbfill'},'-']);
		this.addDocked(paging);
		this.store.reload();
   }
});