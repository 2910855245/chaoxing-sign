"""
学习通班级管理模块

功能:
  - 获取班级列表
  - 获取班级邀请码
  - 加入班级（通过邀请码）
"""
import json
from loguru import logger
from chaoxing.session import ChaoxingSession


def get_class_list(session: ChaoxingSession, course_id: str, cpi: str = None) -> list:
    """获取课程的班级列表

    返回: [{id, name, studentcount, semesterid, classEnc, ...}, ...]
    """
    url = f'https://mobilelearn.chaoxing.com/v2/apis/class/getClassList?courseId={course_id}&fid={session.fid}'
    if cpi:
        url += f'&cpi={cpi}'

    try:
        resp = session.get(url, referer='https://mobilelearn.chaoxing.com/')
        data = resp.json()
    except Exception as e:
        logger.error(f"获取班级列表失败 error={e}")
        return []

    if data.get('result') != 1:
        logger.warning(f"获取班级列表失败 msg={data.get('errorMsg', '未知错误')}")
        return []

    raw_data = data.get('data', {})
    # data 可能是列表或字典
    if isinstance(raw_data, list):
        classes = raw_data
    elif isinstance(raw_data, dict):
        classes = raw_data.get('classArray', raw_data.get('classList', []))
    else:
        classes = []

    logger.info(f"获取班级列表 course_id={course_id} count={len(classes)}")
    return classes


def get_class_invite_code(session: ChaoxingSession, course_id: str, class_id: str,
                          cpi: str = None) -> dict:
    """获取班级邀请码

    返回: {invitecode, validtime, cls2dbcurl}
    """
    url = (f'https://mobilelearn.chaoxing.com/v2/apis/class/getClassInviteCode'
           f'?fid={session.fid}&courseId={course_id}&classId={class_id}')
    if cpi:
        url += f'&cpi={cpi}'

    try:
        resp = session.get(url, referer='https://mobilelearn.chaoxing.com/')
        data = resp.json()
    except Exception as e:
        logger.error(f"获取班级邀请码失败 error={e}")
        return {}

    if data.get('result') != 1:
        logger.warning(f"获取班级邀请码失败 msg={data.get('errorMsg', '未知错误')}")
        return {}

    invite_data = data.get('data', {})
    logger.info(f"获取班级邀请码 class_id={class_id} code={invite_data.get('invitecode')}")
    return invite_data


def get_class_detail(session: ChaoxingSession, course_id: str, class_id: str) -> dict:
    """获取班级详情

    返回: {name, studentcount, creatoruserid, course: {...}, ...}
    """
    url = (f'https://mobilelearn.chaoxing.com/v2/apis/class/getClassDetail'
           f'?classId={class_id}&courseId={course_id}&cpi={session.uid}')

    try:
        resp = session.get(url, referer='https://mobilelearn.chaoxing.com/')
        data = resp.json()
    except Exception as e:
        logger.error(f"获取班级详情失败 error={e}")
        return {}

    if data.get('result') != 1:
        logger.warning(f"获取班级详情失败 msg={data.get('errorMsg', '未知错误')}")
        return {}

    return data.get('data', {})


def join_class_by_code(session: ChaoxingSession, invite_code: str,
                       course_id: str = None, class_id: str = None) -> dict:
    """通过邀请码加入班级

    注意: 学生加入班级的API可能不存在于服务端
    如果API调用失败，建议用户在学习通APP中手动输入邀请码

    返回: {success, message, data}
    """
    url = 'https://mobilelearn.chaoxing.com/v2/apis/class/addStudent'

    # 尝试多种参数格式
    params_list = [
        {'code': invite_code, 'fid': session.fid},
        {'inviteCode': invite_code, 'fid': session.fid},
        {'code': invite_code},
        {'invitecode': invite_code, 'fid': session.fid},
    ]

    # 如果有courseId和classId，添加到参数中
    if course_id and class_id:
        for params in params_list:
            params['courseId'] = course_id
            params['classId'] = class_id

    for params in params_list:
        try:
            # 构建URL参数
            query = '&'.join(f'{k}={v}' for k, v in params.items())
            full_url = f'{url}?{query}'

            resp = session.get(full_url,
                              extra_headers={'X-Requested-With': 'XMLHttpRequest'})
            status = resp.status_code
            text = resp.text()

            if status == 200:
                try:
                    result = json.loads(text)
                    if result.get('result') == 1:
                        logger.info(f"加入班级成功 code={invite_code}")
                        return {'success': True, 'message': '加入成功', 'data': result.get('data')}
                    else:
                        msg = result.get('errorMsg', result.get('msg', '未知错误'))
                        logger.warning(f"加入班级失败 msg={msg}")
                        return {'success': False, 'message': msg}
                except json.JSONDecodeError:
                    if text in ('success', '成功'):
                        return {'success': True, 'message': '加入成功'}
                    return {'success': False, 'message': text}
            elif status == 500:
                # 500错误说明API可能不存在或后端服务问题
                logger.warning(f"加入班级API返回500，可能是教师端API或后端服务问题")
                continue
            else:
                logger.warning(f"加入班级异常 status={status}")
                return {'success': False, 'message': f'HTTP {status}'}
        except Exception as e:
            logger.error(f"加入班级异常 error={e}")
            continue

    # 所有参数格式都失败
    return {
        'success': False,
        'message': '加入班级API暂不可用，请手动在学习通APP中输入邀请码',
        'invite_code': invite_code,
    }


def create_class(session: ChaoxingSession, course_id: str, class_name: str) -> dict:
    """创建新班级

    返回: {success, message, data}
    """
    url = (f'https://mobilelearn.chaoxing.com/v2/apis/class/createClass'
           f'?fid={session.fid}&courseId={course_id}&className={class_name}')

    try:
        resp = session.get(url, referer='https://mobilelearn.chaoxing.com/')
        data = resp.json()
    except Exception as e:
        logger.error(f"创建班级失败 error={e}")
        return {'success': False, 'message': str(e)}

    if data.get('result') == 1:
        logger.info(f"创建班级成功 name={class_name}")
        return {'success': True, 'message': '创建成功', 'data': data.get('data')}
    else:
        msg = data.get('errorMsg', data.get('msg', '未知错误'))
        logger.warning(f"创建班级失败 msg={msg}")
        return {'success': False, 'message': msg}
