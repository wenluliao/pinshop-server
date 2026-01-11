let api = [];
const apiDocListSize = 1
api.push({
    name: 'default',
    order: '1',
    list: []
})
api[0].list.push({
    alias: 'UserController',
    order: '1',
    link: 'user_controller provides_user_management_apis_including_login,_profile,_and_address_management',
    desc: 'User Controller Provides user management APIs including login, profile, and address management',
    list: []
})
api[0].list[0].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/login',
    methodId: 'c93e6144e85db60a9fd74efce9c81187',
    desc: 'User login Supports WeChat openid style login',
});
api[0].list[0].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/profile',
    methodId: '951cab27a958b161c3ca98bac859bf0b',
    desc: 'Get user profile',
});
api[0].list[0].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/profile',
    methodId: 'c77fc8d044f08237b8291b92651d2169',
    desc: 'Update user profile',
});
api[0].list[0].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/addresses',
    methodId: 'a07a930bf360a1565712f2171eec06f2',
    desc: 'Get user address list',
});
api[0].list[0].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/address',
    methodId: '63eaa064275ca0c003a0af157f62d457',
    desc: 'Add user address',
});
api[0].list[0].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/address/{addressId}',
    methodId: 'c54d819961799ebeea23d1012057ce4d',
    desc: 'Update user address',
});
api[0].list[0].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/address/{addressId}',
    methodId: 'ab70bafa0c9482a4d2ed4d61aca35cc2',
    desc: 'Delete user address',
});
api[0].list[0].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/user/address/{addressId}/default',
    methodId: 'a4b0343f64bc5aabedc4b63c01298da0',
    desc: 'Set default address',
});
api[0].list.push({
    alias: 'HealthController',
    order: '2',
    link: '健康检查controller_-_最简单的测试端点',
    desc: '健康检查Controller - 最简单的测试端点',
    list: []
})
api[0].list[1].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/health',
    methodId: '4bb117546d27cecfd8284a001f0f02f0',
    desc: '',
});
api[0].list[1].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/ping',
    methodId: '8e2236f07325a43ab721d4a0dbb21ea1',
    desc: '',
});
api[0].list.push({
    alias: 'ProductController',
    order: '3',
    link: '商品中心-秒杀商品服务  &amp;lt;p&amp;gt;提供秒杀商品列表、详情查询等功能&amp;lt;/p&amp;gt;',
    desc: '商品中心-秒杀商品服务  &amp;lt;p&amp;gt;提供秒杀商品列表、详情查询等功能&amp;lt;/p&amp;gt;',
    list: []
})
api[0].list[2].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/flash-list',
    methodId: '7e2e5c324bbdded38b488c1ae7b113e6',
    desc: '获取秒杀商品流  &lt;p&gt; 返回当前可抢购的秒杀商品列表。 前端用于首页展示秒杀商品卡片。 &lt;/p&gt;',
});
api[0].list[2].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/detail/{skuId}',
    methodId: 'abbb8da5e47475d94afa58e6f04396e5',
    desc: '获取商品详情  &lt;p&gt; 返回指定SKU的商品详细信息。 &lt;/p&gt;',
});
api[0].list[2].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/categories',
    methodId: 'fbac317f84d850cd058ed4d90c65a6fa',
    desc: '获取商品分类  &lt;p&gt; 返回商品分类列表，用于分类导航。 &lt;/p&gt;',
});
api[0].list[2].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/search',
    methodId: 'af2e74c1740c46386696ad0c2a79526f',
    desc: '搜索商品  &lt;p&gt; 根据关键词搜索商品。 &lt;/p&gt;',
});
api[0].list[2].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/product/hot',
    methodId: '8b10fe3db8de46f1b6eb6111033c1217',
    desc: '获取热门商品  &lt;p&gt; 返回销量最高的热门商品列表。 &lt;/p&gt;',
});
api[0].list.push({
    alias: 'SeckillController',
    order: '4',
    link: '交易中心-秒杀服务  &amp;lt;p&amp;gt;提供高并发秒杀抢购功能，采用三级缓存机制保障性能&amp;lt;/p&amp;gt;',
    desc: '交易中心-秒杀服务  &amp;lt;p&amp;gt;提供高并发秒杀抢购功能，采用三级缓存机制保障性能&amp;lt;/p&amp;gt;',
    list: []
})
api[0].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/trade/seckill',
    methodId: 'bd4de0975405c78c776b019f5d8f0f46',
    desc: '执行秒杀下单  &lt;p&gt; 核心高并发接口，采用异步削峰架构。 前端需轮询查询结果。 &lt;/p&gt;  &lt;p&gt; 业务流程： &lt;ul&gt;   &lt;li&gt;1. 本地缓存检查库存（阻挡90%无效请求）&lt;/li&gt;   &lt;li&gt;2. Redis Lua脚本原子扣减库存&lt;/li&gt;   &lt;li&gt;3. 发送MQ消息异步创建订单&lt;/li&gt;   &lt;li&gt;4. 立即返回排队状态&lt;/li&gt; &lt;/ul&gt; &lt;/p&gt;',
});
api[0].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/trade/result/{queueId}',
    methodId: 'a5464b45cecc4711baf31305eb843313',
    desc: '查询秒杀订单状态  &lt;p&gt; 前端通过此接口轮询获取秒杀订单的处理结果。 建议轮询间隔：500ms-1000ms。 &lt;/p&gt;',
});
api[0].list.push({
    alias: 'AdminController',
    order: '5',
    link: 'admin_dashboard_api_controller',
    desc: 'Admin Dashboard API Controller',
    list: []
})
api[0].list[4].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/admin/dashboard',
    methodId: '0126a823a05897f8addafaee0e87565b',
    desc: 'Get dashboard data GET /api/v1/admin/dashboard',
});
api[0].list.push({
    alias: 'PaymentController',
    order: '6',
    link: 'payment_controller provides_payment_creation_and_callback_processing_apis',
    desc: 'Payment Controller Provides payment creation and callback processing APIs',
    list: []
})
api[0].list[5].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/pay/create',
    methodId: '237f6d1e1a3af1ac824bd16642f70bcc',
    desc: 'Create payment',
});
api[0].list[5].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/pay/callback/wechat',
    methodId: '28b4e445eb1aef6d9671aa1df847c8b7',
    desc: 'WeChat payment callback',
});
api[0].list[5].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/pay/callback/alipay',
    methodId: '44065e0ae57104d6b68c21a0a41a7650',
    desc: 'Alipay payment callback',
});
api[0].list[5].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/pay/status',
    methodId: 'dc88321693dedb7f62602aa1d4820c30',
    desc: 'Get payment status',
});
api[0].list.push({
    alias: 'OrderController',
    order: '7',
    link: 'order_controller provides_order_query_and_management_apis',
    desc: 'Order Controller Provides order query and management APIs',
    list: []
})
api[0].list[6].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/order/list',
    methodId: '760e08991a16cc38917ac2d0467288f5',
    desc: 'Get user order list',
});
api[0].list[6].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/order/{orderId}',
    methodId: '18b5ab50f8e9bd37ccd5d96c01056772',
    desc: 'Get order detail',
});
api[0].list[6].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/order/count',
    methodId: '281a1ec945210004d9af6ee2c60edaa5',
    desc: 'Get order count statistics',
});
api[0].list[6].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/order/{orderId}/cancel',
    methodId: 'eb24b47a7b41bfcdcb28bd9c0a84e50b',
    desc: 'Cancel order',
});
api[0].list[6].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/order/{orderId}/status',
    methodId: 'f2947364cb17844b6fd659a433339aec',
    desc: 'Get order status',
});
api[0].list.push({
    alias: 'GroupBuyController',
    order: '8',
    link: '拼团中心-拼团服务  &amp;lt;p&amp;gt;提供拼团发起、参与、查询等功能&amp;lt;/p&amp;gt;',
    desc: '拼团中心-拼团服务  &amp;lt;p&amp;gt;提供拼团发起、参与、查询等功能&amp;lt;/p&amp;gt;',
    list: []
})
api[0].list[7].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/initiate',
    methodId: '3c7842371bd030180adb20f53821a812',
    desc: '发起拼团  &lt;p&gt; 用户成为团长，创建一个新的拼团会话。 &lt;/p&gt;',
});
api[0].list[7].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/join',
    methodId: '80daa1f0edc92263844c72f597b1b94f',
    desc: '参与拼团  &lt;p&gt; 用户加入已有的拼团会话。 &lt;/p&gt;',
});
api[0].list[7].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/{skuId}/sessions',
    methodId: '9eddd75628bbdf86244cc0c4076ca989',
    desc: '获取当前正在拼的团  &lt;p&gt; 返回指定商品正在进行的拼团会话列表。 前端用于展示&quot;还有人在拼单&quot;功能。 &lt;/p&gt;',
});
api[0].list[7].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/my',
    methodId: '24b330539588018f7231e3d24bd8252f',
    desc: '获取我的拼团记录  &lt;p&gt; 返回用户参与的所有拼团记录。 &lt;/p&gt;',
});
api[0].list[7].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:8080/api/v1/group/{sessionId}/detail',
    methodId: '00cc552eeb6ef28bcf43bb97cc5d6b4f',
    desc: '获取拼团详情  &lt;p&gt; 返回指定拼团会话的详细信息，包括所有成员列表。 &lt;/p&gt;',
});
api[0].list.push({
    alias: 'error',
    order: '9',
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