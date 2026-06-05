"""
学习通教师端模块

功能:
  - 创建签到活动
  - 启动签到活动
  - 设置结束时间
  - 结束签到活动
"""
import time
from loguru import logger
from chaoxing.session import ChaoxingSession


def create_sign_active(session: ChaoxingSession, course_id: str, class_id: str,
                       title: str = "签到", other_id: int = 0,
                       latitude: float = None, longitude: float = None,
                       location_range: int = 500) -> dict:
    """创建签到活动（仅创建，不启动）

    Args:
        session: 教师会话
        course_id: 课程ID
        class_id: 班级ID
        title: 活动标题
        other_id: 签到类型 (0=普通, 2=二维码, 3=手势, 4=位置, 5=签到码)
        latitude: 纬度（位置签到时使用）
        longitude: 经度（位置签到时使用）
        location_range: 签到范围（米，默认500）

    返回: {success, active_id, message}
    """
    # 使用 saveOrBegin API，now=0 表示仅保存不启动
    url = 'https://mobilelearn.chaoxing.com/v2/apis/sign/saveOrBegin'
    data = {
        'now': '0',  # 仅保存，不启动
        'courseId': course_id,
        'classId': class_id,
        'cpi': session.uid,
        'fid': session.fid,
        'activeId': '0',
        'activeType': '2',
        'otherId': str(other_id),
        'title': title,
        'timeLong': '600000',
        'lateMinute': '10',
    }

    # 位置签到需要设置坐标
    if other_id == 4 and latitude is not None and longitude is not None:
        data['locationLatitude'] = str(latitude)
        data['locationLongitude'] = str(longitude)
        data['locationRange'] = str(location_range)

    try:
        resp = session.post(url, data=data)
        result = resp.json()
    except Exception as e:
        logger.error(f"创建签到活动失败 error={e}")
        return {'success': False, 'message': str(e)}

    if result.get('result') != 1:
        msg = result.get('errorMsg', result.get('msg', '未知错误'))
        logger.warning(f"创建签到活动失败 msg={msg}")
        return {'success': False, 'message': msg}

    active_id = result.get('data')
    logger.info(f"创建签到活动成功 active_id={active_id}")
    return {'success': True, 'active_id': active_id, 'message': '创建成功'}


def start_sign_active(session: ChaoxingSession, active_id: str,
                      course_id: str, class_id: str) -> dict:
    """启动签到活动

    Args:
        session: 教师会话
        active_id: 活动ID
        course_id: 课程ID
        class_id: 班级ID

    返回: {success, message}
    """
    url = (f'https://mobilelearn.chaoxing.com/v2/apis/active/startActive'
           f'?activeId={active_id}&courseId={course_id}&classId={class_id}'
           f'&activeType=2&fid={session.fid}')

    try:
        resp = session.get(url)
        result = resp.json()
    except Exception as e:
        logger.error(f"启动签到活动失败 error={e}")
        return {'success': False, 'message': str(e)}

    if result.get('result') == 1:
        logger.info(f"启动签到活动成功 active_id={active_id}")
        return {'success': True, 'message': '启动成功'}
    else:
        msg = result.get('errorMsg', result.get('msg', '未知错误'))
        logger.warning(f"启动签到活动失败 msg={msg}")
        return {'success': False, 'message': msg}


def set_sign_end_time(session: ChaoxingSession, active_id: str,
                      duration_minutes: int = 10) -> dict:
    """设置签到活动结束时间

    Args:
        session: 教师会话
        active_id: 活动ID
        duration_minutes: 持续时间（分钟）

    返回: {success, message}
    """
    end_time = int(time.time() * 1000) + duration_minutes * 60 * 1000
    url = (f'https://mobilelearn.chaoxing.com/widget/active/restartActive2'
           f'?DB_STRATEGY=PRIMARY_KEY&STRATEGY_PARA=activeId'
           f'&activeId={active_id}&endTime={end_time}&updateStatus=1')

    try:
        resp = session.get(url)
        result = resp.json()
    except Exception as e:
        logger.error(f"设置结束时间失败 error={e}")
        return {'success': False, 'message': str(e)}

    if result.get('result') == 1:
        logger.info(f"设置结束时间成功 active_id={active_id} duration={duration_minutes}min")
        return {'success': True, 'message': f'设置成功，{duration_minutes}分钟后结束'}
    else:
        msg = result.get('errorMsg', result.get('msg', '未知错误'))
        logger.warning(f"设置结束时间失败 msg={msg}")
        return {'success': False, 'message': msg}


def end_sign_active(session: ChaoxingSession, active_id: str,
                    active_type: int = 2) -> dict:
    """结束签到活动

    Args:
        session: 教师会话
        active_id: 活动ID
        active_type: 活动类型

    返回: {success, message}
    """
    url = (f'https://mobilelearn.chaoxing.com/widget/active/endActive'
           f'?activeId={active_id}&activeType={active_type}&isLockTopic=0')

    try:
        resp = session.get(url)
        result = resp.json()
    except Exception as e:
        logger.error(f"结束签到活动失败 error={e}")
        return {'success': False, 'message': str(e)}

    if result.get('result') == 1:
        logger.info(f"结束签到活动成功 active_id={active_id}")
        return {'success': True, 'message': '结束成功'}
    else:
        msg = result.get('errorMsg', result.get('msg', '未知错误'))
        logger.warning(f"结束签到活动失败 msg={msg}")
        return {'success': False, 'message': msg}


def publish_sign(session: ChaoxingSession, course_id: str, class_id: str,
                 title: str = "签到", duration_minutes: int = 10,
                 other_id: int = 0, latitude: float = None,
                 longitude: float = None, location_range: int = 500) -> dict:
    """一键发布签到

    创建 → 启动 → 设置结束时间

    Args:
        session: 教师会话
        course_id: 课程ID
        class_id: 班级ID
        title: 活动标题
        duration_minutes: 持续时间（分钟）
        other_id: 签到类型 (0=普通, 2=二维码, 3=手势, 4=位置, 5=签到码)
        latitude: 纬度（位置签到时使用）
        longitude: 经度（位置签到时使用）
        location_range: 签到范围（米，默认500）

    返回: {success, active_id, message}
    """
    # 1. 创建活动
    result = create_sign_active(session, course_id, class_id, title, other_id,
                                latitude, longitude, location_range)
    if not result.get('success'):
        return result

    active_id = result.get('active_id')
    if not active_id:
        return {'success': False, 'message': '获取活动ID失败'}

    # 2. 启动活动
    result = start_sign_active(session, active_id, course_id, class_id)
    if not result.get('success'):
        return result

    # 3. 设置结束时间
    result = set_sign_end_time(session, active_id, duration_minutes)
    if not result.get('success'):
        return result

    logger.info(f"一键发布签到成功 active_id={active_id} duration={duration_minutes}min")
    return {'success': True, 'active_id': active_id, 'message': f'发布成功，{duration_minutes}分钟后结束'}


def get_active_list_teacher(session: ChaoxingSession, course_id: str,
                            class_id: str) -> list:
    """获取教师端活动列表

    返回: [{activeId, title, status, activeType, otherId, createTime}, ...]
    """
    url = (f'https://mobilelearn.chaoxing.com/v2/apis/active/pcActivelist'
           f'?fid={session.fid}&courseId={course_id}&classId={class_id}')

    try:
        resp = session.get(url)
        data = resp.json()
    except Exception as e:
        logger.error(f"获取活动列表失败 error={e}")
        return []

    active_list = data.get('data', {}).get('activeList', [])
    activities = []
    for item in active_list:
        activities.append({
            'activeId': item.get('activeId'),
            'title': item.get('title', ''),
            'status': item.get('status', 0),
            'activeType': item.get('activeType', 0),
            'otherId': item.get('otherId', 0),
            'createTime': item.get('createTime', ''),
        })

    logger.info(f"获取活动列表 course_id={course_id} count={len(activities)}")
    return activities


def _get_latest_active_id(session: ChaoxingSession, course_id: str,
                          class_id: str) -> str:
    """获取最新创建的活动ID"""
    activities = get_active_list_teacher(session, course_id, class_id)
    if activities:
        return str(activities[0]['activeId'])
    return None
