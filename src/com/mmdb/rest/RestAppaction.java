package com.mmdb.rest;

import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.springframework.stereotype.Component;

import com.mmdb.rest.category.CiCateRest;
import com.mmdb.rest.category.KpiCateRest;
import com.mmdb.rest.category.RelCateRest;
import com.mmdb.rest.category.ViewCateRest;
import com.mmdb.rest.dataset.DatasetRest;
import com.mmdb.rest.db.HandleDataBaseRest;
import com.mmdb.rest.download.FileDownload;
import com.mmdb.rest.event.EventRest;
import com.mmdb.rest.event.EventViewRest;
import com.mmdb.rest.font.FontRest;
import com.mmdb.rest.icon.ImageRest;
import com.mmdb.rest.icon.UserIconRest;
import com.mmdb.rest.icon.ViewIconRest;
import com.mmdb.rest.info.CiInfoRest;
import com.mmdb.rest.info.KpiInfoRest;
import com.mmdb.rest.info.ServerInfoRest;
import com.mmdb.rest.info.ViewInfoRest;
import com.mmdb.rest.info.ViewPortfolioRest;
import com.mmdb.rest.jmx.MonitorActiveMq;
import com.mmdb.rest.mapping.CiCiMappingRest;
import com.mmdb.rest.mapping.CiMappingRest;
import com.mmdb.rest.mapping.KpiMappingRest;
import com.mmdb.rest.mapping.PerfDbMappingRest;
import com.mmdb.rest.mapping.RelMappingRest;
import com.mmdb.rest.performance.PerformanceRestService;
import com.mmdb.rest.performance.PerformanceViewRestService;
import com.mmdb.rest.relation.CiKpiRest;
import com.mmdb.rest.relation.CiRelRest;
import com.mmdb.rest.relation.CiViewRelRest;
import com.mmdb.rest.role.CompanyRestService;
import com.mmdb.rest.role.DeptRestService;
import com.mmdb.rest.role.ModuleRestService;
import com.mmdb.rest.role.RoleManagerService;
import com.mmdb.rest.role.RoleRestService;
import com.mmdb.rest.role.UserRestService;
import com.mmdb.rest.rule.LinkRuleRest;
import com.mmdb.rest.subscription.SubscriptionPortfolioViewRest;
import com.mmdb.rest.subscription.SubscriptionViewRest;
import com.mmdb.rest.subscription.UserProfileRest;
import com.mmdb.rest.task.TaskRest;

@Component
public class RestAppaction extends org.restlet.Application {
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());

		// CiCategory
		router.attach("/cicate", CiCateRest.class);
		router.attach("/cicate/{param1}", CiCateRest.class);

		// relCategory
		router.attach("/relcate", RelCateRest.class);
		router.attach("/relcate/{param1}", RelCateRest.class);

		// ciInfo
		router.attach("/ci", CiInfoRest.class);
		router.attach("/ci/{param1}", CiInfoRest.class);
		router.attach("/ci/{param1}/{param2}", CiInfoRest.class);

		// ciRelation ci的关系
		router.attach("/cirel", CiRelRest.class);
		router.attach("/cirel/{param1}", CiRelRest.class);

		// viewIcon 自定义图标
		router.attach("/viewicon", ViewIconRest.class);
		router.attach("/viewicon/{param1}", ViewIconRest.class);

		// viewCategory视图目录管理
		router.attach("/viewcate", ViewCateRest.class);
		router.attach("/viewcate/{param1}", ViewCateRest.class);

		// viewInfomation视图管理
		router.attach("/view", ViewInfoRest.class);
		router.attach("/view/{param1}", ViewInfoRest.class);
		router.attach("/view/{param1}/{param2}", ViewInfoRest.class);

		// viewInfomation组合视图管理
		router.attach("/portfolio", ViewPortfolioRest.class);
		router.attach("/portfolio/{param1}", ViewPortfolioRest.class);
		router.attach("/portfolio/{param1}/{param2}", ViewPortfolioRest.class);

		// image 图标管理的图标
		router.attach("/icon", ImageRest.class);
		router.attach("/icon/{param1}", ImageRest.class);
		router.attach("/icon/{param1}/{param2}", ImageRest.class);

		// 文件下载
		router.attach("/download/{param1}", FileDownload.class);

		// db 数据库配置
		router.attach("/database", HandleDataBaseRest.class);
		router.attach("/database/{param1}", HandleDataBaseRest.class);
		router.attach("/database/{param1}/{param2}", HandleDataBaseRest.class);

		// 数据集配置
		router.attach("/dataset", DatasetRest.class);
		router.attach("/dataset/{param1}", DatasetRest.class);
		router.attach("/dataset/{param1}/{param2}", DatasetRest.class);

		// 内部分类映射
		router.attach("/cimapping", CiCiMappingRest.class);
		router.attach("/cimapping/{param1}", CiCiMappingRest.class);
		router.attach("/cimapping/{param1}/{param2}", CiCiMappingRest.class);

		// 外部分类映射
		router.attach("/catemapping", CiMappingRest.class);
		router.attach("/catemapping/{param1}", CiMappingRest.class);
		router.attach("/catemapping/{param1}/{param2}", CiMappingRest.class);

		// 外部关系映射
		router.attach("/relmapping", RelMappingRest.class);
		router.attach("/relmapping/{param1}", RelMappingRest.class);
		router.attach("/relmapping/{param1}/{param2}", RelMappingRest.class);

		// 性能数据集映射
		router.attach("/perfmapping", PerfDbMappingRest.class);
		router.attach("/perfmapping/{param1}", PerfDbMappingRest.class);
		router.attach("/perfmapping/{param1}/{param2}", PerfDbMappingRest.class);

		// 任务
		router.attach("/task", TaskRest.class);
		router.attach("/task/{param1}", TaskRest.class);
		router.attach("/task/{param1}/{param2}", TaskRest.class);

		// 规则引擎
		router.attach("/linkRule", LinkRuleRest.class);
		router.attach("/linkRule/{param1}", LinkRuleRest.class);
		router.attach("/linkRule/{param1}/{param2}", LinkRuleRest.class);

		// KpiCategory
		router.attach("/kpicate", KpiCateRest.class);
		router.attach("/kpicate/{param1}", KpiCateRest.class);

		router.attach("/kpi", KpiInfoRest.class);
		router.attach("/kpi/{param1}", KpiInfoRest.class);

		router.attach("/kpici", CiKpiRest.class);
		router.attach("/kpici/{param1}", CiKpiRest.class);
		router.attach("/kpici/{param1}/{param2}", CiKpiRest.class);
		router.attach("/kpici/{param1}/{param2}/{param3}", CiKpiRest.class);

		// 权限管理
		router.attach("/user", UserRestService.class);
		router.attach("/user/{name}", UserRestService.class);

		// 用户图片管理
		router.attach("/usericon", UserIconRest.class);

		router.attach("/module", ModuleRestService.class);
		router.attach("/module/{name}", ModuleRestService.class);

		router.attach("/role", RoleRestService.class);
		router.attach("/role/{name}", RoleRestService.class);

		router.attach("/company", CompanyRestService.class);
		router.attach("/company/{name}", CompanyRestService.class);

		router.attach("/dept", DeptRestService.class);
		router.attach("/dept/{name}", DeptRestService.class);

		// 认证管理
		router.attach("/login", RoleManagerService.class);
		router.attach("/logout", RoleManagerService.class);
		router.attach("/operation/{name}", RoleManagerService.class);

		// 性能管理
		router.attach("/performance", PerformanceRestService.class);
		router.attach("/performance/{chart}", PerformanceRestService.class);

		// 性能视图
		router.attach("/performanceview", PerformanceViewRestService.class);
		router.attach("/performanceview/{viewName}",
				PerformanceViewRestService.class);

		// 事件视图
		router.attach("/eventview", EventViewRest.class);
		router.attach("/eventview/{param1}", EventViewRest.class);

		// ServerInfoRest
		router.attach("/serverinfo", ServerInfoRest.class);

		// 事件操作
		router.attach("/event", EventRest.class);
		router.attach("/event/{param1}", EventRest.class);

		// ci与视图的关系
		router.attach("/relation", CiViewRelRest.class);
		router.attach("/relation/{param1}", CiViewRelRest.class);
		router.attach("/relation/{param1}/{param2}", CiViewRelRest.class);

		// 订阅相关
		// 视图订阅
		router.attach("/subscription/view", SubscriptionViewRest.class);
		router.attach("/subscription/view/{param1}", SubscriptionViewRest.class);
		router.attach("/subscription/view/{param1}/{param2}",
				SubscriptionViewRest.class);

		// 组合视图订阅
		router.attach("/subscription/portfolio",
				SubscriptionPortfolioViewRest.class);
		router.attach("/subscription/portfolio/{param1}",
				SubscriptionPortfolioViewRest.class);
//		router.attach("/subscription/portfolio/{param1}/{param2}",
//				SubscriptionPortfolioViewRest.class);

		// kpi同步映射
		router.attach("/kpimapping", KpiMappingRest.class);
		router.attach("/kpimapping/{param1}", KpiMappingRest.class);
		router.attach("/kpimapping/{param1}/{param2}", KpiMappingRest.class);

		// 字体
		router.attach("/font", FontRest.class);
		// amq监控
		router.attach("/activemq", MonitorActiveMq.class);
		router.attach("/activemq/{param1}/{param2}", MonitorActiveMq.class);

		router.attach("/userprofile", UserProfileRest.class);
		router.attach("/userprofile/{param1}", UserProfileRest.class);
		
		return router;
	}
}
