<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="bootstrap.min.css"
	rel="stylesheet">
<link
	href="bootstrap-responsive.min.css"
	rel="stylesheet">
<link href="docs.css" rel="stylesheet">
<script src="jquery.min.js"></script>
<script src="bootstrap.min.js"></script>
<script src="bootstrap.file-input.js"></script>
<style type="text/css">
.key {
	list-style: none;
	width: 350px;
	float: left;
}

.value {
	list-style: none;
	clear: right;
}

select {
	margin-bottom: 10px;
	margin-top: 10px;
	margin-right: 10px;
}

.dobut {
	margin-bottom:10px;
	margin-top:10px;
}

.file {
	margin-bottom:10px;
	margin-left:30px;
}

.submit {
	width:10%;
	margin-left:110px;
}

.topos {
	margin-left:25px;
}

.addsel {
	margin-left:74px;
}

.badge-extract {
	width:7px;
}

input[type="text"]{
	margin-bottom:0px;
}

</style>
<script type="text/javascript">
function ensureInt(n) {
    var isInt = /^\d+$/.test(n);
    if (!isInt) {
        alert("'" + n + "' is not integer.");
    }

    return isInt;
}
$(document).ready(function(){
	$('input[type=file]').bootstrapFileInput();
	$("#topos").change(function(){
		var topoJSON = this.value;
		if (topoJSON != "") {
			topoJSON= topoJSON.replace(/'/g, "\"");
			var topo = jQuery.parseJSON(topoJSON);
			$(".dobut").remove();
			$("#toponame").remove();
			$("#topos").prepend("<option id='toponame' value='0'>"+topo.name+"</option>");
			var html="";
			switch(topo.status){
				case "ACTIVE":
					html+= "<button class='btn btn-primary dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='inactive'>暂停</button>&nbsp;&nbsp;&nbsp;&nbsp;";
					html+="<button class='btn btn-warning dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='reblance'>睡眠</button>&nbsp;&nbsp;&nbsp;&nbsp;";
					html+="<button class='btn btn-danger dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='kill'>停止</button>";
					break;
				case "INACTIVE":
					html += "<button class='btn btn-primary dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='active'>复活</button>&nbsp;&nbsp;&nbsp;&nbsp;";
					html+="<button class='btn btn-danger dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='kill'>停止</button>";
					break;
				case "REBALANCING":
					html += "<button class='btn btn-primary dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='active'>复活</button>&nbsp;&nbsp;&nbsp;&nbsp;";
					html+="<button class='btn btn-warning dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='inactive'>继续睡眠</button>&nbsp;&nbsp;&nbsp;&nbsp;";
					html+="<button class='btn btn-danger dobut' style='width:100px' placehoder='"+topo.name+"' type='button' id='kill'>停止</button>";
					break;
				default:
			}
			$(html).insertAfter($(this));
			$("#toponame").attr("selected", true);
		}
	});
	
	$(".badge-success").click(function(){
		var html="<span><br><select name='config' class='addsel configs'>";
		$(".configs option").each(function(){
			html+="<option value="+$(this).text()+">"+$(this).val()+"</option>";
		});
		html+="</select>值：<input type='text' class='configVal' name='configVal' value=''/>&nbsp;&nbsp;<span class='badge badge-extract'>-</span></span>";
		$(this).after(html);
	});
	
	$(document).on("click",".badge-extract",function(){
		$(this).parent().remove();
	});
	
	$(document).on("click",".dobut",function(){
		alert("aa");
		var toponame=$(this).attr("placehoder");
		var event=$(this).attr("id");
		if(toponame!=""){
			var opts = {
			    type:'POST',
			    url:event+".do?name="+toponame
			};
			var doit=false;
			if(event=="reblance"||event=="kill"){
				var waitSecs = prompt('你将在以下时间到达时（秒）' + (event=="reblance"?"重新运行":"退出运行")+toponame,30);
				if (waitSecs != null && waitSecs != "" && ensureInt(waitSecs)) {
	  				opts.url += '&wait=' + waitSecs;
	  				doit=true;
				} else {
	  				return false;
				}
			}else{
				doit=confirm('你确定' + (event=="active"?"激活":"暂停运行")+toponame+"吗？");
			}
			if(doit){
				$.ajax(opts).always(function () {
			        window.location.reload();
			    }).fail(function () {
			        alert("Error while communicating with Nimbus.");
			    });
			}
		}
	});
});
</script>
<title>控制台</title>
</head>
<body data-spy="scroll" data-target=".bs-docs-sidebar">
	<div class="container">
		<div class="row">
			<div class="span3 bs-docs-sidebar">
				<ul class="nav nav-list bs-docs-sidenav affix">
					<li class="active"><a href="#status"><i class="icon-chevron-right"></i> 拓扑列表</a></li>
				</ul>
			</div>
			<div class="span9">
				<section id="status">
					<div class="page-header">
						<h1>拓扑列表</h1>
						<button class="btn btn-primary" id="stop" width="100px" type="button">停止storm</button>
						<button class="btn btn-primary" id="start" width="100px" type="button">启动storm</button>
						<button class="btn btn-primary" id="restart" width="100px" type="button">重启storm</button>
					</div>
					<div>
						<span>列表：</span>
						<select class="topos" id="topos">
							<!-- <option value="">API</option> -->
							<c:forEach items="${status }" var="o">
								<option value="${o }">${o }</option>
							</c:forEach>
						</select>
					</div>
					<div class="">
						<form target="hideWin" action="${base}/uploadJar.do" method="post" enctype="multipart/form-data">
	    					选择：<input type="file" name="jar" class="file" accept="application/java-archive" data-filename-placement="inside" title="jar包"></input></br>
							main方法：<input type="text" name="mainMethod" id="mainMethod" title="main方法"/></br>
							作业名称： <input type="text" name="jobName" id="jobName" title="作业名称"/>
							<div>
								<span>参数列表：</span>
								<select name="config" class="configs">
									<c:forEach items="${configs }" var="o">
										<option value="${o }">${o }</option>
									</c:forEach>
								</select>值：<input type="text" class="configVal" name="configVal" value=""/>&nbsp;&nbsp;<span class="badge badge-success">+</span>
							</div>
							<button class="btn btn-lg btn-primary btn-block submit" width="100px" type="submit">提交</button>
	    				</form>
					</div>
				</section>
			</div>
		</div>
	</div>
</body>
</html>