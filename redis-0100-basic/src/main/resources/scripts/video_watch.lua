-- 视频学习记录逻辑处理脚本

-- 相关Key/Value
-- 节、小节视频信息
-- cc:lesson:#lessonId:video -> (hash) {videoId:#videoId,videoSec:#videoSec}
-- cc:lv:#lvId:video -> (hash) {videoId:#videoId,videoSec:#videoSec,lessonId:#lessonId}
-- 视频观看统计信息(来源，记录最后一次的，详情见历史记录中)
-- cc:uid:#userId:rid:#recruitId:lid:#lessonId:watch -> (hash) {studyTotalTime:#studyTotalTime,watchState:#watchState,watchCount:#watchCount,sourseType:#sourseType,createTime:createTime}
-- cc:uid:#userId:rid:#recruitId:lvid:#lessonVideoId:watch -> [同上]
-- 视频观看历史记录(watchTime是观看时间，studyTime是当次学习时长)
-- cc:uid:#userId:rid:#recruitId:lid:#lessonId:watch:history -> (list) [{watchTime:#watchTime,studyTime:#studyTime,sourceType:#sourceType},{},...]
-- 用户、招生下观看视频数
-- cc:uid:#userId:rid:#recruitId:videos -> (string) #watch_video_number

-- 接收参数
local userId ,recruitId ,lessonId ,learningTime = KEYS[1] ,KEYS[2] ,KEYS[3] ,KEYS[4]
local sourseType ,createTime = ARGV[1] ,ARGV[2]

-- 组装出Key
local key = 'cc:uid' .. userId .. ':rid:' .. recruitId .. ':lid:' .. lessonId .. ':watch'

-- 获取视频总时长
local videoSec = redis.call('HGET' ,'cc:lesson:' .. lessonId .. ':video' ,'videoSec')
-- 如果未获取到视频总时长，返回-1
if not videoSec then return -1 end

-- 累加学习总时长
local studyTotalTime = redis.call('HINCRBY' ,key ,'studyTotalTime' ,learningTime)
-- 学习进度百分比(学习总时长 / 视频总时长，最大值为1)
local percent = studyTotalTime / videoSec
-- 获取视频总时间，判断是否已完成观看(超过50%就算完成观看)
if percent >= 0.5 then
  -- 如果观看状态字段不存在，才更正观看完成状态
  if redis.call('HEXISTS' ,key ,'watchState') == 0 then
    -- 完成观看后，将观看状态置为已观看
    redis.call('HSET' ,key ,'watchState' ,1)
    -- 将视频观看信息Key作为消息，发布到cc:channel:video:watch:complete频道上(订阅端将获取消息，将观看记录写入数据库)
    redis.call('PUBLISH' ,'cc:channel:video:watch:complete' ,key)
    -- 更新用户、招生观看视频数
    redis.call('INCR' ,'cc:uid:' .. userId .. ':rid:' .. recruitId .. ':videos')
  end
end

-- 观看次数自增
redis.call('HINCRBY' ,key ,'watchCount' ,1)

-- 写入创建时间(第一次观看时间)
if createTime then
  redis.call('HSETNX' ,key ,'createTime' ,createTime)
end

-- 写入来源信息(PC/IOS/Android)
if sourseType then
  redis.call('HSET' ,key ,'sourseType' ,sourseType)
end

-- 记录观看历史日志(TODO 未实现)
local historyKey = key .. ':history'

-- 返回观看百分比
if percent > 0 then
  return percent < 1 and percent or 1
end