(function(Graph){


	"use strict";


	/**
	 * 居中
	 * @param {Object} margin
	 */
	Graph.prototype.zoomToCenter = function(margin){
		var bounds = null;
		var count = 0;
		if(this.getChildCells().length >= 1){
			this.refresh();
			this.zoomActual();

			bounds = this.getGraphBounds();
			margin = margin || 10;

			this.view.setTranslate(
					-bounds.x -(bounds.width - this.container.clientWidth)/ 2,
					-bounds.y - (bounds.height - this.container.clientHeight) / 2
			);

			while( ((bounds.width + margin * 2) > this.container.clientWidth
				|| (bounds.height + margin * 2) > this.container.clientHeight )
				&& ++count < 30){
				this.zoomOut();
				bounds = this.getGraphBounds();
			}
		}
	};


	/**
	 * Sets the link for the given cell.
	 */
	Graph.prototype.setViewForCell = function(cell, view)
	{
		var value = null;

		if (cell.value != null && typeof(cell.value) == 'object')
		{
			value = cell.value.cloneNode(true);
		}
		else
		{
			var doc = mxUtils.createXmlDocument();

			value = doc.createElement('UserObject');
			value.setAttribute('label', cell.value);
		}

		if (view != null && view.length > 0)
		{
			value.setAttribute('view', view);
		}
		else
		{
			value.removeAttribute('view');
		}

		this.model.setValue(cell, value);
	};

	/**
	 * Returns the link for the given cell.
	 */
	Graph.prototype.getViewForCell = function(cell)
	{
		if (cell.value != null && typeof(cell.value) == 'object')
		{
			return cell.value.getAttribute('view');
		}

		return null;
	};



	/**
	 * Sets the queryid for the given cell.
	 */
	Graph.prototype.setQueryIdForCell = function(cell, queryid)
	{
		var value = null;

		if (cell.value != null && typeof(cell.value) == 'object')
		{
			value = cell.value.cloneNode(true);
		}
		else
		{
			var doc = mxUtils.createXmlDocument();

			value = doc.createElement('UserObject');
			value.setAttribute('label', cell.value);
		}

		if (queryid != null && queryid.length > 0)
		{
			value.setAttribute('queryid', queryid);
		}
		else
		{
			value.removeAttribute('queryid');
		}

		this.model.setValue(cell, value);
	};

	/**
	 * Returns the queryid for the given cell.
	 */
	Graph.prototype.getQueryIdForCell = function(cell)
	{
		if (cell.value != null && typeof(cell.value) == 'object')
		{
			return cell.value.getAttribute('queryid');
		}

		return null;
	};

	Graph.prototype.getCellById = function(id)
	{
		var cells, i, ret = null;

		cells = this.getDepCells();

		for(i=0; i<cells.length; i++) {
			if (cells[i].id === id) {
				ret = cells[i];
				break;
			}
		}

		return ret;
	};

	Graph.prototype.getCiCell = function(){
		var list = [];
		var cells = editor.graph.getDepVertexs();

		$.each(cells, function(){
			if(Unit.isCiCell(this)){
				list.push(this);
			}
		});

		return list;
	};


	/**
	 * Sets the link for the given cell.
	 */
	Graph.prototype.setOriStyle = function(cell, style)
	{
		var value = null;

		if (cell.value != null && typeof(cell.value) == 'object')
		{
			value = cell.value.cloneNode(true);
		}
		else
		{
			var doc = mxUtils.createXmlDocument();

			value = doc.createElement('UserObject');
			value.setAttribute('label', cell.value);
		}

		if (style != null && style.length > 0)
		{
			value.setAttribute('oristyle', style);
		}
		else
		{
			value.removeAttribute('oristyle');
		}

		this.model.setValue(cell, value);
	};

	/**
	 * Returns the link for the given cell.
	 */
	Graph.prototype.getOriStyle = function(cell)
	{
		if (cell.value != null && typeof(cell.value) == 'object')
		{
			return cell.value.getAttribute('oristyle');
		}

		return null;
	};



	Graph.prototype.setCellAttr = function(cell, key, val)
	{
		var value = null;

		if (cell.value != null && typeof(cell.value) == 'object')
		{
			value = cell.value.cloneNode(true);
		}
		else
		{
			var doc = mxUtils.createXmlDocument();

			value = doc.createElement('UserObject');
			value.setAttribute('label', cell.value);
		}

		if (val != null && val.length > 0)
		{
			value.setAttribute(key, val);
		}
		else
		{
			value.removeAttribute(key);
		}

		this.model.setValue(cell, value);
	};


	Graph.prototype.getCellAttr = function(cell, key)
	{
		if (cell.value != null && typeof(cell.value) == 'object')
		{
			return cell.value.getAttribute(key);
		}

		return null;
	};

	/**
	 * Sets the link for the given cell.
	 */
	Graph.prototype.setLightEdge = function(cell, v)
	{
		var value = null;

		if (cell.value != null && typeof(cell.value) == 'object')
		{
			value = cell.value.cloneNode(true);
		}
		else
		{
			var doc = mxUtils.createXmlDocument();

			value = doc.createElement('UserObject');
			value.setAttribute('label', cell.value);
		}

		if (v != null && v.length > 0)
		{
			value.setAttribute('light', v);
		}
		else
		{
			value.removeAttribute('light');
		}

		this.model.setValue(cell, value);
	};

	/**
	 * Returns the link for the given cell.
	 */
	Graph.prototype.getLightEdge = function(cell)
	{
		if (cell.value != null && typeof(cell.value) == 'object')
		{
			return cell.value.getAttribute('light');
		}

		return null;
	};

	Graph.prototype.getDepVertexs = function() {
		var cells, loop, _this = this, ret = [];

		cells = _this.getChildVertices();

		loop = function(list){
			var i, child = null;
			for(i=0; i<list.length; i++){
				ret.push(list[i]);
				child = _this.getChildVertices(list[i]);
				if(child && child.length >= 1){
					loop(child);
				}
			}
		};

		loop(cells);

		return ret;
	};


	Graph.prototype.getDepEdges = function() {
		var cells, loop, _this = this, ret = [];

		cells = _this.getDepVertexs();
		cells.push(_this.getDefaultParent());

		loop = function(list){
			var i = 0,
				ii = 0,
				child = null;
			for(i=0; i<list.length; i++){
				child = _this.getChildEdges(list[i]);
				for(ii=0; ii<child.length; ii++){
					ret.push(child[ii]);
				}
			}
		};

		loop(cells);

		return ret;
	};

	Graph.prototype.getDepCells = function() {
		var vertexs, edges, _this = this, i, ret = [];

		vertexs = _this.getDepVertexs();
		for(i=0; i<vertexs.length; i++){
			ret.push(vertexs[i]);
		}

		edges = _this.getDepEdges();
		for(i=0; i<edges.length; i++){
			ret.push(edges[i]);
		}

		return ret;
	};

	Graph.prototype.addLight = function(cell, pstyle, color){
		var edgeStyle, light, style, oriWidth, ret, _this = this;

		color = color ? color : '#0066FF';
		pstyle = pstyle ? pstyle : "";
		edgeStyle = pstyle + 'strokeColor=' + color + ';strokeWidth=4;fillColor=none;';
		if(cell && cell.vertex){
			light = _this.insertVertex(
				cell.getParent(),
				"light_" + cell.id,
				"",
				cell.geometry.x,
				cell.geometry.y,
				cell.geometry.width,
				cell.geometry.height,
				edgeStyle
			);
			_this.orderCells(true, [light]);
		}else if(cell && cell.edge){
			style = cell.getStyle();
			style = style ? style : "";
			ret =  /strokeWidth=(\w+)/.exec(style);
			if(ret && ret[0]){
				oriWidth = ret[1];
			    style = style.replace(
			        ret[0],
			        "strokeWidth=3"
			    );
			}else{
				oriWidth = "1";
				if(style && style[style.length-1] != ";"){
					style += ";";
				}
				style += "strokeWidth=3;";
			}

			_this.setLightEdge(cell, oriWidth);
		    cell.setStyle(style);
		    _this.refresh(cell);
		}

		return light;
	};

	Graph.prototype.removeLight = function(){
		var cells, i, removeCells = [], oriWidth, restore, _this = this;

		restore = function(cell, width){
			var style, ret;

			style = cell.getStyle();
			style = style ? style : "";
			ret =  /strokeWidth=(\w+)/.exec(style);
			if(ret && ret[0]){
			    style = style.replace(
			        ret[0],
			        "strokeWidth=" + width
			    );
			}else{
				if(style && style[style.length-1] != ";"){
					style += ";";
				}
				style += "strokeWidth="+width+";";
			}

			_this.setLightEdge(cell, "");
		    cell.setStyle(style);
		    _this.refresh(cell);
		};

		cells = _this.getDepCells();
		for(i=0; i<cells.length; i++){
			oriWidth = _this.getLightEdge(cells[i]);
			if(cells[i].vertex && cells[i].id.indexOf("light_") === 0){
				removeCells.push(cells[i]);
			}else if(cells[i].edge && oriWidth){
				restore(cells[i], oriWidth);
			}
		}

		_this.removeCells(removeCells);
		//_this.refresh();
	};


	Graph.prototype.getCellAt = function(x, y, parent, vertices, edges) {
		vertices = (vertices != null) ? vertices : true;
		edges = (edges != null) ? edges : true;
		parent = (parent != null) ? parent : this.getDefaultParent();

		if (parent != null)
		{
			var childCount = this.model.getChildCount(parent);

			for (var i = childCount - 1; i >= 0; i--)
			{
				var cell = this.model.getChildAt(parent, i);
				if(cell.id.indexOf("light_") >= 0 || cell.id.indexOf("in_") >= 0){
					continue;
				}

				var result = this.getCellAt(x, y, cell, vertices, edges);

				if (result != null)
				{
					return result;
				}
				else if (this.isCellVisible(cell) && (edges && this.model.isEdge(cell) ||
					vertices && this.model.isVertex(cell)))
				{
					var state = this.view.getState(cell);

					if (this.intersects(state, x, y))
					{
						return cell;
					}
				}
			}
		}

		return null;
	};

	Graph.prototype.getSelectionEdges = function(){
		var arr = [],
			cells = [];

		arr = this.getSelectionCells();
		$.each(arr, function(){
			if(this.edge){
				cells.push(this);
			}
		});

		return cells;
	};


	Graph.prototype.getJson = function(){
		var _this = this, cells, map, getItem, originScale, gv, originTranslace, o, getImage, pre, getLevel, svg = [],
			svgMap = {};


		var getCellValue = function (cell) {
			if (cell) {
				return typeof cell.value === 'object' ? _this.getCellAttr(cell, 'label') : cell.value;
			} else {
				return '';
			}
		};

		pre = function(name){
			return name;
		};

		getImage = function(cell){
			var o = _this.getCellStyle(cell);
			if(o.image){
				return o.image;
			}else{

				if(o.shape === 'label'){
					if(o.rounded){
						return pre('rounded');
					}else{
						return pre(o.shape);
					}
				}else if(o.shape === 'ellipse') {
					return pre(o.shape);
				}else if(o.shape === 'doubleEllipse'){
					return pre(o.shape);
				}else if(o.shape === 'triangle'){
					return pre(o.shape);
				}else if(o.shape === 'rhombus'){
					return pre(o.shape);
				}else if(o.shape === 'hexagon'){
					return pre(o.shape);
				}else if(o.shape === 'actor'){
					return pre(o.shape);
				}else if(o.shape === 'cloud'){
					return pre(o.shape);
				}else if(o.shape === 'cylinder') {
					return pre(o.shape);
				}else if(o.shape === 'xor') {
					return pre(o.shape);
				}else if(o.shape === 'or') {
					return pre(o.shape);
				}else if(o.shape === 'step') {
					return pre(o.shape);
				}else if(o.shape === 'tape') {
					return pre(o.shape);
				}else if(o.shape === 'cube') {
					return pre(o.shape);
				}else if(o.shape === 'note') {
					return pre(o.shape);
				}else if(o.shape === 'folder') {
					return pre(o.shape);
				}else if(o.shape === 'card') {
					return pre(o.shape);
				}else if(o.shape === 'plus') {
					return pre(o.shape);
				}else if(o.shape === 'arrow') {
					return pre(o.shape);
				}
			}

		};

		o = {
			graphWidth: $(_this.container).width(),
			graphHeight: $(_this.container).height(),
			nodes: [],
			containers: [],
			edges: []
		};

		getLevel = function(cell){
			var level = 0;

			while(cell.getParent().id !== "1"){
				cell = cell.getParent();
				level++;
			}

			return level;
		};

		getItem = function(id){
			var obj, i;

			for(i in map){
				if(map[i].cell.id === id){
					obj = map[i];
					break;
				}
			}

			return obj;
		};

		gv =  _this.getView();
		originScale = gv.getScale();
		originTranslace = {
			x: gv.getTranslate().x,
			y: gv.getTranslate().y
		};

		_this.zoomToCenter();
		map = _this.getView().getStates().map;
		cells = _this.getDepCells();

		o.scale = gv.getScale();

		$.each(cells, function(){
			var item = getItem(this.id),
				level,
				tmp = {};

            if(item){
                if((this.vertex && this.getChildCount() === 0 && _this.getCellStyle(this).shape !== 'line') || _this.getCellStyle(this).shape === 'arrow'){
                    var img = getImage(this);

					o.nodes.push({
                        img: img,
                        name: getCellValue(this),
                        id: this.id,
                        width: item.width,
                        height: item.height,
                        x: item.x,
                        y: item.y
                    });

					if(!svgMap[img]){
						svgMap[img] = true;
						svg.push(img);
					}
                }

				if(_this.isContainer(this)){
					level = getLevel(this);
					o.containers.push({
						name: getCellValue(this),
						id: this.id,
						width: item.width,
						height: item.height,
						level: level,
						x: item.x,
						y: item.y
					})
				}

				if(this.edge && _this.getCellStyle(this).shape !== 'arrow'){
					tmp.sourceName = getCellValue(_this, this.source);
					tmp.sourceId = this.source ? this.source.id : null;
					tmp.targetName = getCellValue(_this, this.target);
					tmp.targetId = this.target ? this.target.id : null;
					tmp.points = [];
					$.each(item.absolutePoints, function(){
						var _this = this;
						tmp.points.push({
							x: _this.x,
							y: _this.y
						});
					});
					o.edges.push(tmp);
				}

            }

		});

		gv.setScale(originScale);
		gv.setTranslate(originTranslace.x, originTranslace.y);

		return o;
	};

	Graph.prototype.isCellFoldable = function(cell)
	{
		return cell.isCollapsed() || this.isSwimlane(cell);
	};

})(Graph);
