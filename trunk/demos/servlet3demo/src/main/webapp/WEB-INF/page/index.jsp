<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="./common/include.jsp"%>

<html>
<head>
<title>任务列表</title>
<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
</head>
<body width="960px">
	<table width="100%" border="1">
		<thead>
			<tr>
				<td width="150px">任务名</td>
				<td width="80px">添加者</td>
				<td width="80px">添加时间</td>
				<td width="300px">任务描述</td>
				<td>任务URL例子</td>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td width="150px">HBase插入性能测试</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-4-7</td>
				<td width="300px">测试HBase插入的性能。自动提交，插2个列簇。table：resource_01001</td>
				<td><a target="blank" href="${basePath}task/hBasePutProfiler?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">HBase主键随机查询性能测试</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-4-8</td>
				<td width="300px">HBase主键随机查询性能测试。三个列簇。table：resource_01012</td>
				<td><a target="blank" href="${basePath}task/hBaseGetProfiler?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">MySQL插入性能测试</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-4-7</td>
				<td width="300px">测试MySQL插入的性能。没有事务。</td>
				<td><a target="blank" href="${basePath}task/mySQLInsertProfiler?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">通用资源新建性能测试</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-4-8</td>
				<td width="300px">测试通用资源的新建性能。资源类型=01012</td>
				<td><a target="blank" href="${basePath}task/commonResourceNewProfiler?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">redis列表插入性能测试</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-4-18</td>
				<td width="300px">测试redis的list的插入性能。</td>
				<td><a target="blank" href="${basePath}task/redisListPushProfiler?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">redis列表移除性能测试</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-4-18</td>
				<td width="300px">测试redis的list的移除性能。</td>
				<td><a target="blank" href="${basePath}task/redisListPopProfiler?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">刘凤玉测试资源批量创建</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-5-10</td>
				<td width="300px">测试通用资源的新建性能。资源类型=01001</td>
				<td><a target="blank" href="${basePath}task/commonResourceNewProfiler01001?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">刘凤玉测试资源批量创建</td>
				<td width="80px">刘凤玉</td>
				<td width="80px">2013-5-10</td>
				<td width="300px">测试通用资源的新建性能。资源类型=01001</td>
				<td><a target="blank" href="${basePath}task/commonResourceNewProfiler01001?threadCount=1&loopCount=100">执行</a></td>
			</tr>
		</tbody>
		<tbody>
			<tr>
				<td width="150px">杨剑飞测试资源批量创建</td>
				<td width="80px">杨剑飞</td>
				<td width="80px">2013-5-10</td>
				<td width="300px">非批量任务，执行一次，添加一条资源，该接口用于ab测试。资源类型=01001</td>
				<td><a target="blank" href="${basePath}profile/resource/new">测试</a></td>
			</tr>
		</tbody>
	</table>
	<div>
		<script>
		  (function() {
		    var cx = '004255448246691416050:q9ena7b3num';
		    var gcse = document.createElement('script');
		    gcse.type = 'text/javascript';
		    gcse.async = true;
		    gcse.src = (document.location.protocol == 'https:' ? 'https:' : 'http:') +
		        '//www.google.com/cse/cse.js?cx=' + cx;
		    var s = document.getElementsByTagName('script')[0];
		    s.parentNode.insertBefore(gcse, s);
		  })();
		</script>
		<gcse:search></gcse:search>
	</div>
</body>
</html>