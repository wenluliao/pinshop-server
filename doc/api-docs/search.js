let api = [];
const apiDocListSize = 1
api.push({
    name: 'default',
    order: '1',
    list: []
})
api[0].list.push({
    alias: 'ProductController',
    order: '1',
    link: '商品中心-秒杀商品服务  &amp;lt;p&amp;gt;提供秒杀商品列表、详情查询等功能&amp;lt;/p&amp;gt;',
    desc: '商品中心-秒杀商品服务  &amp;lt;p&amp;gt;提供秒杀商品列表、详情查询等功能&amp;lt;/p&amp;gt;',
    list: []
})
api[0].list[0].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/flash-list',
    methodId: '7e2e5c324bbdded38b488c1ae7b113e6',
    desc: '获取秒杀商品流  &lt;p&gt; 返回当前可抢购的秒杀商品列表。 前端用于首页展示秒杀商品卡片。 &lt;/p&gt;',
});
api[0].list[0].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/detail/{skuId}',
    methodId: 'abbb8da5e47475d94afa58e6f04396e5',
    desc: '获取商品详情  &lt;p&gt; 返回指定SKU的商品详细信息。 &lt;/p&gt;',
});
api[0].list.push({
    alias: 'SeckillController',
    order: '2',
    link: '交易中心-秒杀服务  &amp;lt;p&amp;gt;提供高并发秒杀抢购功能，采用三级缓存机制保障性能&amp;lt;/p&amp;gt;',
    desc: '交易中心-秒杀服务  &amp;lt;p&amp;gt;提供高并发秒杀抢购功能，采用三级缓存机制保障性能&amp;lt;/p&amp;gt;',
    list: []
})
api[0].list[1].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/trade/seckill',
    methodId: 'bd4de0975405c78c776b019f5d8f0f46',
    desc: '执行秒杀下单  &lt;p&gt; 核心高并发接口，采用异步削峰架构。 前端需轮询查询结果。 &lt;/p&gt;  &lt;p&gt; 业务流程： &lt;ul&gt;   &lt;li&gt;1. 本地缓存检查库存（阻挡90%无效请求）&lt;/li&gt;   &lt;li&gt;2. Redis Lua脚本原子扣减库存&lt;/li&gt;   &lt;li&gt;3. 发送MQ消息异步创建订单&lt;/li&gt;   &lt;li&gt;4. 立即返回排队状态&lt;/li&gt; &lt;/ul&gt; &lt;/p&gt;',
});
api[0].list[1].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/trade/result/{queueId}',
    methodId: 'a5464b45cecc4711baf31305eb843313',
    desc: '查询秒杀订单状态  &lt;p&gt; 前端通过此接口轮询获取秒杀订单的处理结果。 建议轮询间隔：500ms-1000ms。 &lt;/p&gt;',
});
api[0].list.push({
    alias: 'AdminController',
    order: '3',
    link: 'admin_dashboard_api_controller',
    desc: 'Admin Dashboard API Controller',
    list: []
})
api[0].list[2].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/admin/dashboard',
    methodId: '0126a823a05897f8addafaee0e87565b',
    desc: 'Get dashboard data GET /api/v1/admin/dashboard',
});
api[0].list.push({
    alias: 'GroupBuyController',
    order: '4',
    link: '拼团中心-拼团服务  &amp;lt;p&amp;gt;提供拼团发起、参与、查询等功能&amp;lt;/p&amp;gt;',
    desc: '拼团中心-拼团服务  &amp;lt;p&amp;gt;提供拼团发起、参与、查询等功能&amp;lt;/p&amp;gt;',
    list: []
})
api[0].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/initiate',
    methodId: '3c7842371bd030180adb20f53821a812',
    desc: '发起拼团  &lt;p&gt; 用户成为团长，创建一个新的拼团会话。 &lt;/p&gt;',
});
api[0].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/join',
    methodId: '80daa1f0edc92263844c72f597b1b94f',
    desc: '参与拼团  &lt;p&gt; 用户加入已有的拼团会话。 &lt;/p&gt;',
});
api[0].list[3].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/{skuId}/sessions',
    methodId: '9eddd75628bbdf86244cc0c4076ca989',
    desc: '获取当前正在拼的团  &lt;p&gt; 返回指定商品正在进行的拼团会话列表。 前端用于展示&quot;还有人在拼单&quot;功能。 &lt;/p&gt;',
});
api[0].list.push({
    alias: 'error',
    order: '5',
    link: 'error_code_list',
    desc: '错误码列表',
    list: []
})
document.onkeydown = keyDownSearch;
function keyDownSearch(e) {
    const theEvent = e;
    const code = theEvent.keyCode || theEvent.which || theEvent.charCode;
    if (code === 13) {
        const search = document.getElementById('search');
        const searchValue = search.value.toLocaleLowerCase();

        let searchGroup = [];
        for (let i = 0; i < api.length; i++) {

            let apiGroup = api[i];

            let searchArr = [];
            for (let i = 0; i < apiGroup.list.length; i++) {
                let apiData = apiGroup.list[i];
                const desc = apiData.desc;
                if (desc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                    searchArr.push({
                        order: apiData.order,
                        desc: apiData.desc,
                        link: apiData.link,
                        alias: apiData.alias,
                        list: apiData.list
                    });
                } else {
                    let methodList = apiData.list || [];
                    let methodListTemp = [];
                    for (let j = 0; j < methodList.length; j++) {
                        const methodData = methodList[j];
                        const methodDesc = methodData.desc;
                        if (methodDesc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                            methodListTemp.push(methodData);
                            break;
                        }
                    }
                    if (methodListTemp.length > 0) {
                        const data = {
                            order: apiData.order,
                            desc: apiData.desc,
                            link: apiData.link,
                            alias: apiData.alias,
                            list: methodListTemp
                        };
                        searchArr.push(data);
                    }
                }
            }
            if (apiGroup.name.toLocaleLowerCase().indexOf(searchValue) > -1) {
                searchGroup.push({
                    name: apiGroup.name,
                    order: apiGroup.order,
                    list: searchArr
                });
                continue;
            }
            if (searchArr.length === 0) {
                continue;
            }
            searchGroup.push({
                name: apiGroup.name,
                order: apiGroup.order,
                list: searchArr
            });
        }
        let html;
        if (searchValue === '') {
            const liClass = "";
            const display = "display: none";
            html = buildAccordion(api,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        } else {
            const liClass = "open";
            const display = "display: block";
            html = buildAccordion(searchGroup,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        }
        const Accordion = function (el, multiple) {
            this.el = el || {};
            this.multiple = multiple || false;
            const links = this.el.find('.dd');
            links.on('click', {el: this.el, multiple: this.multiple}, this.dropdown);
        };
        Accordion.prototype.dropdown = function (e) {
            const $el = e.data.el;
            let $this = $(this), $next = $this.next();
            $next.slideToggle();
            $this.parent().toggleClass('open');
            if (!e.data.multiple) {
                $el.find('.submenu').not($next).slideUp("20").parent().removeClass('open');
            }
        };
        new Accordion($('#accordion'), false);
    }
}

function buildAccordion(apiGroups, liClass, display) {
    let html = "";
    if (apiGroups.length > 0) {
        if (apiDocListSize === 1) {
            let apiData = apiGroups[0].list;
            let order = apiGroups[0].order;
            for (let j = 0; j < apiData.length; j++) {
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#' + apiData[j].alias + '">' + apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                html += '<ul class="sectlevel2" style="'+display+'">';
                let doc = apiData[j].list;
                for (let m = 0; m < doc.length; m++) {
                    let spanString;
                    if (doc[m].deprecated === 'true') {
                        spanString='<span class="line-through">';
                    } else {
                        spanString='<span>';
                    }
                    html += '<li><a href="#' + doc[m].methodId + '">' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                }
                html += '</ul>';
                html += '</li>';
            }
        } else {
            for (let i = 0; i < apiGroups.length; i++) {
                let apiGroup = apiGroups[i];
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#_'+apiGroup.order+'_' + apiGroup.name + '">' + apiGroup.order + '.&nbsp;' + apiGroup.name + '</a>';
                html += '<ul class="sectlevel1">';

                let apiData = apiGroup.list;
                for (let j = 0; j < apiData.length; j++) {
                    html += '<li class="'+liClass+'">';
                    html += '<a class="dd" href="#' + apiData[j].alias + '">' +apiGroup.order+'.'+ apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                    html += '<ul class="sectlevel2" style="'+display+'">';
                    let doc = apiData[j].list;
                    for (let m = 0; m < doc.length; m++) {
                       let spanString;
                       if (doc[m].deprecated === 'true') {
                           spanString='<span class="line-through">';
                       } else {
                           spanString='<span>';
                       }
                       html += '<li><a href="#' + doc[m].methodId + '">'+apiGroup.order+'.' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                   }
                    html += '</ul>';
                    html += '</li>';
                }

                html += '</ul>';
                html += '</li>';
            }
        }
    }
    return html;
}