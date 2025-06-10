/*
省,手机型号,  guid, level  event_id,      url,session_id,action_time
a,   mi6,    1  ,   2    app_launch,     \N    s001    ,  t1       0     0
a,   mi6,    1  ,   2    page_view,      /p1   s001    ,  t2       0     0
a,   mi6,    1  ,   2    page_view,      /p2   s001    ,  t3       0     0
a,   mi6,    1  ,   2    item_share,     /p2   s001    ,  t4       0     0
a,   mi6,    1  ,   2    push_back,      /p2   s001    ,  t5       0     0

a,   mi6,    1  ,   2    wakeed_up,      /p2   s001    ,  t15      1     1
a,   mi6,    1  ,   2    page_view,      /p3   s001    ,  t16      0     1
a,   mi6,    1  ,   2    app_close,      /p3   s001    ,  t19      0     1

b,   mi6,    2  ,   3    app_launch,     \N    s002    ,  t1
b,   mi6,    2  ,   3    page_view,      /p1   s002    ,  t3
b,   mi6,    2  ,   3    page_view,      /p5   s002    ,  t6
b,   mi6,    2  ,   3    item_share,     /p5   s002    ,  t8
b,   mi6,    2  ,   3    page_view,      /p2   s002    ,  t10
b,   mi6,    2  ,   3    app_close,      /p2   s002    ,  t20


a,   mi6,    1  ,   2    app_launch,     \N    s003    ,  t51
a,   mi6,    1  ,   2    page_view,      /p1   s003    ,  t52
a,   mi6,    1  ,   2    app_close,      /p1   s003    ,  t53

--
province,device_type,level, pv数, uv数,访问总时长，会话总数,跳出会话数


province,device_type,level, session_id, guid,   pv数, 访问总时长
   江西     mi6         2      s001       1      6        12
                              s002       2      20       28

province,device_type,level, session_id, guid, flag,  pv数, 访问总时长
   江西     mi6         2      s001       1     1       6        12
                              s001       1     2       20       28
                              s002       2     1       20       28

*/

-- 注册函数
create temporary function bm_intagg as 'top.doe.hive.dataware.Bigint2BitmapAggFunction';
create temporary function bm_count as 'top.doe.hive.dataware.BitmapCountFunction';
create temporary function bm_union as 'top.doe.hive.dataware.BitmapUnionAggFunction';
create temporary function bm_show as 'top.doe.hive.dataware.BitmapShowFunction';

with tmp as (select device_type,
                    app_version,
                    gps_province,
                    gps_city,
                    gps_region,
                    gender,
                    member_level_id,
                    page_type,
                    event_id,
                    session_id,  --  度量,维度
                    guid,        --  度量,维度
                    action_time, --  度量
                    sum(if(event_id = 'wake_up', 1, 0)) over (partition by guid,session_id order by action_time) as flag
             from dwd.user_action_log_detail
             where dt = '20241101')

select device_type,
       app_version,
       gps_province,
       gps_city,
       gps_region,
       gender,
       member_level_id,
       page_type,
       sum(pv)                    as pv,
       sum(session_timelong)         timelong_amt,
       sum(is_jump)               as jump_session_amt,
       count(distinct session_id) as session_amt,
       bm_intagg(guid)            as uv_bitmap
from (select device_type,
             app_version,
             gps_province,
             gps_city,
             gps_region,
             gender,
             member_level_id,
             page_type,
             guid,
             session_id,
             sum(pv)                as pv,
             sum(timelong_part)        session_timelong,
             if(sum(pv) >= 2, 0, 1) as is_jump
      from (select device_type,
                   app_version,
                   gps_province,
                   gps_city,
                   gps_region,
                   gender,
                   member_level_id,
                   page_type,
                   guid,
                   session_id,
                   flag,
                   count(if(event_id = 'page_view', 1, null)) as pv,
                   max(action_time) - min(action_time)        as timelong_part
            from tmp
            group by device_type,
                     app_version,
                     gps_province,
                     gps_city,
                     gps_region,
                     gender,
                     member_level_id,
                     page_type,
                     guid,
                     session_id,
                     flag) o1
      group by device_type,
               app_version,
               gps_province,
               gps_city,
               gps_region,
               gender,
               member_level_id,
               page_type,
               guid,
               session_id) o2

group by device_type,
         app_version,
         gps_province,
         gps_city,
         gps_region,
         gender,
         member_level_id,
         page_type