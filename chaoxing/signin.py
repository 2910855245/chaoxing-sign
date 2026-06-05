"""
学习通签到模块 -- 支持全部5种签到类型

签到类型 (otherId):
  0 = 普通签到
  2 = 二维码签到
  3 = 手势签到
  4 = 位置签到
  5 = 签到码签到

核心思路:
  - 普通/位置: 直接签
  - 手势/签到码: checkSignCode API 暴力破解（无限制），同时提示用户询问
  - 二维码: analysis API 获取 enc
"""
import re
import time
import threading
from itertools import permutations
from loguru import logger
from chaoxing.session import ChaoxingSession

# 签到类型映射 (otherId -> 名称)
SIGN_TYPES = {
    0: "普通签到",
    2: "二维码签到",
    3: "手势签到",
    4: "位置签到",
    5: "签到码签到",
}

# 需要暴力破解的类型
BRUTE_FORCE_TYPES = {3, 5}

# ─── 手势字典 ───

GESTURE_DICT_4 = [
    # 直线
    "1234", "2345", "3456", "4567", "5678", "6789",
    "1470", "2580", "3690", "1590", "3570",
    "7890", "1230", "4560", "1478", "2589", "1597", "3571",
    "7896", "3694", "1236", "4569", "7893",
    # L 形
    "1478", "1479", "1489", "2589", "3698", "3697",
    "7896", "7893", "7410", "8520", "9630",
    "1475", "1472", "3695", "3692",
    "7410", "7413", "9630", "9631",
    "1230", "1236", "7890", "7894",
    # Z 形
    "1235", "1236", "1475", "1476", "3215", "3214",
    "7895", "7894", "9875", "9876",
    "1258", "1259", "3258", "3254",
    "7852", "7851", "9852", "9856",
    # 对角线
    "1590", "3570", "1593", "3571", "1597", "3579",
    "1594", "1596", "3574", "3572",
    # N/M/W 形
    "1458", "1459", "2569", "2568",
    "3658", "3657", "1456", "3654",
    # U 形
    "14789", "14569", "36987", "32587",
    "12589", "32569", "74123", "96321",
    # 常见解锁
    "1598", "1596", "3574", "3572",
    "1235", "1475", "7895", "3695",
    "1258", "1458", "3658", "7858",
    "1590", "2580", "3570", "4560",
    "1254", "1256", "3254", "3256",
    "7854", "7856", "9854", "9856",
    "1452", "1456", "3652", "3654",
    "7458", "7452", "9658", "9654",
]

GESTURE_DICT_5 = [
    # 直线
    "12345", "23456", "34567", "45678", "56789",
    "14789", "25896", "36987", "15963", "35741",
    "78963", "12369", "14785", "25896", "36987",
    "12347", "12348", "12349", "12358", "12359",
    "14569", "14589", "14789", "15698", "15987",
    "32147", "32159", "32547", "32569", "32587",
    "36541", "36587", "36987", "35741", "35789",
    "74123", "74125", "74159", "74569", "74589",
    "78541", "78563", "78963", "78941", "78951",
    "96321", "96325", "96541", "96587", "98741",
    "98753", "98763", "98741", "98541", "98563",
    # Z 形
    "12357", "12358", "12359", "12368", "12369",
    "32157", "32158", "32159", "32148", "32147",
    "78951", "78952", "78953", "78962", "78963",
    "98751", "98752", "98753", "98742", "98741",
    "12589", "12587", "12569", "12567",
    "32589", "32587", "32569", "32567",
    "78521", "78523", "78541", "78543",
    "98521", "98523", "98541", "98543",
    # S 形
    "12365", "12364", "12369", "32145", "32147",
    "78965", "78963", "98745", "98741",
    "12547", "12548", "12549", "12569", "12567",
    "32547", "32548", "32549", "32569", "32567",
    "78541", "78542", "78543", "78563", "78561",
    "98541", "98542", "98543", "98563", "98561",
    # L 形
    "14789", "14785", "14786", "25896", "25897",
    "36987", "36984", "74123", "74125", "74126",
    "85236", "85231", "96321", "96325", "96324",
    "14789", "14785", "14786", "14782", "14783",
    "36987", "36984", "36981", "36982", "36985",
    "74123", "74125", "74126", "74128", "74129",
    "96321", "96325", "96324", "96327", "96328",
    # U 形
    "14789", "14569", "36987", "32587",
    "12589", "32569", "74123", "96321",
    "14785", "14563", "36985", "32541",
    "12547", "32541", "78523", "98541",
    # 对角线
    "15963", "15987", "35741", "35789",
    "15963", "35741", "15987", "35789",
    "15963", "15987", "15964", "15962",
    "35741", "35789", "35742", "35748",
    # 常见组合
    "12369", "14785", "15963", "32147", "35741",
    "78963", "74125", "96321", "98741",
    "12357", "12358", "12359", "12365", "12368",
    "32157", "32159", "32148", "32145",
    "78951", "78953", "78962", "78965",
    "98751", "98753", "98742", "98745",
    "14528", "14529", "14536", "14539",
    "36528", "36527", "36514", "36517",
    "74528", "74521", "74536", "74531",
    "96528", "96521", "96514", "96517",
    "12547", "12548", "12549", "12569",
    "32547", "32548", "32549", "32569",
    "78541", "78542", "78543", "78563",
    "98541", "98542", "98543", "98563",
]

# 去重
GESTURE_DICT_4 = list(dict.fromkeys(GESTURE_DICT_4))
GESTURE_DICT_5 = list(dict.fromkeys(GESTURE_DICT_5))


def get_active_list(session: ChaoxingSession, course_id: str, class_id: str) -> list:
    """获取课程的签到活动列表"""
    ts = int(time.time() * 1000)
    url = (f"https://mobilelearn.chaoxing.com/v2/apis/active/student/activelist"
           f"?fid=0&courseId={course_id}&classId={class_id}&_={ts}")

    try:
        resp = session.get(url, referer="https://mobilelearn.chaoxing.com/")
        data = resp.json()
    except Exception as e:
        logger.error(f"获取签到活动列表失败 error={e}")
        return []

    active_list = data.get("data", {}).get("activeList", [])
    if not active_list:
        return []

    activities = []
    for item in active_list:
        other_id = item.get("otherId", -1)
        # 转为整数（API 返回的可能是字符串）
        try:
            other_id = int(other_id)
        except (ValueError, TypeError):
            other_id = -1
        if item.get("activeType") != 2:
            continue

        # 注意：活动列表API返回的userStatus可能是错误的
        # 需要使用活动详情API获取正确的userStatus
        user_status = item.get("userStatus", 0)

        act = {
            "activeId": item.get("id"),
            "otherId": other_id,
            "typeName": SIGN_TYPES.get(other_id, f"未知({other_id})"),
            "nameOne": item.get("nameOne", ""),
            "startTime": item.get("startTime", 0),
            "status": item.get("status", 0),
            "statusText": _status_text(item.get("status", 0)),
            "userStatus": user_status,
            "courseId": course_id,
            "classId": class_id,
        }
        activities.append(act)

    logger.info(f"获取签到活动列表 course_id={course_id} count={len(activities)}")
    return activities


def get_active_detail(session: ChaoxingSession, active_id: str) -> dict:
    """获取签到活动详情（正确的userStatus）"""
    url = f'https://mobilelearn.chaoxing.com/v2/apis/active/getPPTActiveInfo?activeId={active_id}'
    try:
        resp = session.get(url, referer="https://mobilelearn.chaoxing.com/")
        data = resp.json()
        return data.get('data', {})
    except Exception as e:
        logger.error(f"获取活动详情失败 error={e}")
        return {}


def check_real_sign_status(session: ChaoxingSession, active_id: str,
                           course_id: str = None, class_id: str = None) -> bool:
    """检查真实的签到状态

    优先使用 activelist API（准确），回退到 detail API。
    注意：getPPTActiveInfo API 对位置签到返回错误的 userStatus。
    """
    # 如果有课程信息，用 activelist API（更准确）
    if course_id and class_id:
        acts = get_active_list(session, course_id, class_id)
        for a in acts:
            if str(a['activeId']) == str(active_id):
                return a.get('userStatus') == 1

    # 回退到详情 API
    detail = get_active_detail(session, active_id)
    user_status = detail.get('userStatus')
    return user_status == 1


def get_attend_list(session: ChaoxingSession, active_id: str) -> dict:
    """获取签到活动的签到列表（老师端）

    返回: {yiqianList: [...], weiqianList: [...]}
    """
    url = f'https://mobilelearn.chaoxing.com/widget/sign/pcTeaSignController/getAttendList?activeId={active_id}'
    try:
        resp = session.get(url, referer="https://mobilelearn.chaoxing.com/")
        data = resp.json()
        if data.get('result') == 1:
            return data.get('data', {})
        return {}
    except Exception as e:
        logger.error(f"获取签到列表失败 error={e}")
        return {}


def get_active_detail(session: ChaoxingSession, active_id: str) -> dict:
    """获取签到活动详情"""
    url = f"https://mobilelearn.chaoxing.com/v2/apis/active/getPPTActiveInfo?activeId={active_id}"
    try:
        resp = session.get(url, referer="https://mobilelearn.chaoxing.com/")
        data = resp.json()
        return data.get("data", {})
    except Exception as e:
        logger.error(f"获取活动详情失败 activeId={active_id} error={e}")
        return {}


def _status_text(status: int) -> str:
    return {0: "未签到", 1: "已签到", 2: "已过期"}.get(status, "未知")


# ─── 核心 API ───

def check_sign_code(session: ChaoxingSession, active_id: str, sign_code: str) -> bool:
    """校验手势/签到码是否正确"""
    url = (f"https://mobilelearn.chaoxing.com/widget/sign/pcStuSignController/checkSignCode"
           f"?activeId={active_id}&signCode={sign_code}")
    try:
        resp = session.get(url, referer="https://mobilelearn.chaoxing.com/")
        data = resp.json()
        return data.get("result") == 1
    except Exception:
        return False


def get_enc_from_analysis(session: ChaoxingSession, active_id: str) -> str:
    """通过 analysis API 获取 enc（二维码签到）"""
    url1 = f"https://mobilelearn.chaoxing.com/pptSign/analysis?vs=1&DB_STRATEGY=RANDOM&aid={active_id}"
    try:
        resp1 = session.get(url1, referer="https://mobilelearn.chaoxing.com/")
        text = resp1.text()
    except Exception as e:
        logger.error(f"analysis 请求失败 error={e}")
        return ""

    match = re.search(r"'([a-f0-9]{32})'", text)
    if not match:
        match = re.search(r"code=\\\\?'([a-f0-9]{32})", text)
    if not match:
        logger.warning(f"无法提取 code activeId={active_id}")
        return ""

    code = match.group(1)
    url2 = f"https://mobilelearn.chaoxing.com/pptSign/analysis2?DB_STRATEGY=RANDOM&code={code}"
    try:
        resp2 = session.get(url2, referer="https://mobilelearn.chaoxing.com/")
        enc = resp2.text().strip()
        if enc and enc != "success" and len(enc) > 5:
            return enc
    except Exception:
        pass
    return ""


def pre_sign(session: ChaoxingSession, active_id: str,
             course_id: str, class_id: str) -> dict:
    """预签到"""
    url = (f"https://mobilelearn.chaoxing.com/newsign/preSign"
           f"?courseId={course_id}&classId={class_id}"
           f"&activePrimaryId={active_id}&general=1&sys=1&ls=1&appType=15"
           f"&uid={session.uid}&ut=s")
    try:
        session.get(url, referer="https://mobilelearn.chaoxing.com/")
        return {"ok": True}
    except Exception as e:
        logger.error(f"预签到失败 error={e}")
        return {}


def stu_sign(session: ChaoxingSession, params: dict) -> dict:
    """执行签到请求"""
    base_url = "https://mobilelearn.chaoxing.com/pptSign/stuSignajax"
    query = "&".join(f"{k}={v}" for k, v in params.items())
    url = f"{base_url}?{query}"
    extra_headers = {
        "Referer": "https://mobilelearn.chaoxing.com/",
        "X-Requested-With": "XMLHttpRequest",
    }
    try:
        resp = session.get(url, referer="https://mobilelearn.chaoxing.com/",
                           extra_headers=extra_headers)
        text = resp.text()
    except Exception as e:
        return {"success": False, "message": str(e)}

    if text in ("success", "您已签到过了"):
        return {"success": True, "message": "签到成功"}
    if text == "success2":
        return {"success": False, "message": "签到已过期"}
    if text == "validate":
        return {"success": False, "message": "需要验证码"}
    return {"success": False, "message": text}


# ─── 暴力破解（带中断支持）───

class BruteForceState:
    """暴力破解状态管理"""
    def __init__(self):
        self.found = None
        self.stop_event = threading.Event()
        self.attempted = 0
        self.total = 0

    def stop(self):
        self.stop_event.set()

    def is_stopped(self):
        return self.stop_event.is_set()

    def set_found(self, code):
        self.found = code
        self.stop_event.set()


def brute_force_gesture_async(session: ChaoxingSession, active_id: str,
                               number_count: int = 5,
                               on_progress=None) -> BruteForceState:
    """异步暴力破解手势（可在外部中断）"""
    state = BruteForceState()

    def _worker():
        dict_key = f"GESTURE_DICT_{number_count}"
        dictionary = globals().get(dict_key, [])

        # Phase 1: 字典
        logger.info(f"手势暴力 Phase1: 字典 {len(dictionary)} 个")
        for i, code in enumerate(dictionary):
            if state.is_stopped():
                return
            if check_sign_code(session, active_id, code):
                logger.info(f"字典命中 signCode={code}")
                state.set_found(code)
                return
            state.attempted = i + 1
            if on_progress:
                on_progress(f"字典 {i+1}/{len(dictionary)}")
            time.sleep(0.03)

        # Phase 2: 全排列
        points = "123456789"
        total = 1
        for k in range(number_count, 0, -1):
            total *= (10 - k)
        state.total = total

        logger.info(f"手势暴力 Phase2: 全排列 {total} 种")
        tried = set(dictionary)
        count = 0

        for perm in permutations(points, number_count):
            if state.is_stopped():
                return
            code = "".join(perm)
            if code in tried:
                continue
            tried.add(code)
            count += 1

            if check_sign_code(session, active_id, code):
                logger.info(f"全排列命中 signCode={code}")
                state.set_found(code)
                return

            state.attempted = len(dictionary) + count
            if count % 50 == 0:
                if on_progress:
                    on_progress(f"枚举 {count}/{total}")
                time.sleep(0.01)

        logger.warning(f"手势暴力失败 尝试了{count}种")

    t = threading.Thread(target=_worker, daemon=True)
    t.start()
    return state


def brute_force_code_async(session: ChaoxingSession, active_id: str,
                            max_digits: int = 4,
                            on_progress=None) -> BruteForceState:
    """异步暴力破解签到码（可在外部中断）"""
    state = BruteForceState()

    def _worker():
        for digits in [4, 5, 6]:
            if state.is_stopped():
                return
            total = 10 ** digits
            logger.info(f"签到码暴力 {digits}位 共{total}种")

            for i in range(total):
                if state.is_stopped():
                    return
                code = str(i).zfill(digits)

                if check_sign_code(session, active_id, code):
                    logger.info(f"签到码命中 signCode={code}")
                    state.set_found(code)
                    return

                state.attempted = i + 1
                if i % 100 == 0:
                    if on_progress:
                        on_progress(f"{digits}位 {i}/{total}")
                    time.sleep(0.01)

            logger.warning(f"{digits}位签到码暴力失败")

    t = threading.Thread(target=_worker, daemon=True)
    t.start()
    return state


# ─── 签到执行 ───

def sign_activity(session: ChaoxingSession, activity: dict,
                  manual_code: str = None,
                  on_progress=None) -> dict:
    """签到单个活动

    manual_code: 用户手动提供的手势/签到码（可选）
    """
    active_id = str(activity["activeId"])
    other_id = activity["otherId"]
    course_id = activity["courseId"]
    class_id = activity["classId"]

    # 使用活动详情API检查真实的签到状态
    real_signed = check_real_sign_status(session, active_id)
    if real_signed:
        return {"success": True, "message": "已签过"}

    if activity.get("status") == 2:
        return {"success": False, "message": "已过期"}

    pre_sign(session, active_id, course_id, class_id)

    if other_id == 0:
        return _sign_normal(session, active_id)
    elif other_id == 4:
        # 位置签到需要在APP端手动完成
        return {"success": False, "message": "位置签到需要在APP端完成", "need_app": True, "type": "位置签到"}
    elif other_id == 3:
        return _sign_gesture(session, active_id, manual_code=manual_code,
                             on_progress=on_progress)
    elif other_id == 5:
        return _sign_code(session, active_id, manual_code=manual_code,
                          on_progress=on_progress)
    elif other_id == 2:
        return _sign_qrcode(session, active_id)
    else:
        return _sign_normal(session, active_id)


def sign_location_with_coords(session: ChaoxingSession, active_id: str,
                              course_id: str, class_id: str,
                              latitude: float, longitude: float,
                              address: str = "") -> dict:
    """位置签到（带坐标）

    用于APP端提交正确的GPS坐标
    """
    pre_sign(session, active_id, course_id, class_id)
    params = {
        "activeId": active_id, "uid": session.uid, "clientip": "",
        "latitude": str(latitude), "longitude": str(longitude),
        "appType": "15", "fid": session.fid,
        "name": session.get_user_info().get("name", ""),
        "ifTiJiao": "1",
    }
    result = stu_sign(session, params)
    return {**result, "type": "位置签到"}


def _sign_normal(session, active_id) -> dict:
    params = {
        "activeId": active_id, "uid": session.uid, "clientip": "",
        "latitude": "-1", "longitude": "-1", "appType": "15",
        "fid": session.fid, "name": session.get_user_info().get("name", ""),
    }
    result = stu_sign(session, params)
    return {**result, "type": "普通签到"}


def _sign_location(session, active_id) -> dict:
    params = {
        "activeId": active_id, "uid": session.uid, "clientip": "",
        "latitude": "-1", "longitude": "-1", "appType": "15",
        "fid": session.fid, "name": session.get_user_info().get("name", ""),
        "ifTiJiao": "1",
    }
    result = stu_sign(session, params)
    return {**result, "type": "位置签到"}


def _sign_gesture(session, active_id, manual_code=None,
                  on_progress=None) -> dict:
    """手势签到"""
    if manual_code:
        # 用户提供了手势码，直接签
        params = {
            "activeId": active_id, "uid": session.uid, "clientip": "",
            "latitude": "-1", "longitude": "-1", "appType": "15",
            "fid": session.fid, "name": session.get_user_info().get("name", ""),
            "signCode": manual_code,
        }
        result = stu_sign(session, params)
        return {**result, "type": "手势签到", "signCode": manual_code}

    # 暴力破解
    detail = get_active_detail(session, active_id)
    number_count = detail.get("numberCount", 5)
    state = brute_force_gesture_async(session, active_id, number_count,
                                      on_progress=on_progress)
    return {"success": False, "message": "暴力破解中...", "type": "手势签到",
            "_brute_state": state, "_sign_fn": _sign_gesture}


def _sign_code(session, active_id, manual_code=None,
               on_progress=None) -> dict:
    """签到码签到"""
    if manual_code:
        params = {
            "activeId": active_id, "uid": session.uid, "clientip": "",
            "latitude": "-1", "longitude": "-1", "appType": "15",
            "fid": session.fid, "name": session.get_user_info().get("name", ""),
            "signCode": manual_code,
        }
        result = stu_sign(session, params)
        return {**result, "type": "签到码签到", "signCode": manual_code}

    state = brute_force_code_async(session, active_id, on_progress=on_progress)
    return {"success": False, "message": "暴力破解中...", "type": "签到码签到",
            "_brute_state": state, "_sign_fn": _sign_code}


def _sign_qrcode(session, active_id) -> dict:
    enc = get_enc_from_analysis(session, active_id)
    if not enc:
        return {"success": False, "message": "无法获取enc", "type": "二维码签到"}

    params = {
        "enc": enc, "name": session.get_user_info().get("name", ""),
        "activeId": active_id, "uid": session.uid, "clientip": "",
        "latitude": "-1", "longitude": "-1", "fid": session.fid, "appType": "15",
    }
    result = stu_sign(session, params)
    return {**result, "type": "二维码签到"}


def sign_with_code(session: ChaoxingSession, activity: dict, code: str) -> dict:
    """用用户提供的 code 直接签到（中断暴力破解后调用）"""
    active_id = str(activity["activeId"])
    other_id = activity["otherId"]
    course_id = activity["courseId"]
    class_id = activity["classId"]

    pre_sign(session, active_id, course_id, class_id)

    params = {
        "activeId": active_id, "uid": session.uid, "clientip": "",
        "latitude": "-1", "longitude": "-1", "appType": "15",
        "fid": session.fid, "name": session.get_user_info().get("name", ""),
        "signCode": code,
    }
    result = stu_sign(session, params)
    type_name = SIGN_TYPES.get(other_id, "未知")
    return {**result, "type": type_name, "signCode": code}


def check_and_sign(session: ChaoxingSession, course_id: str, class_id: str,
                   manual_code: str = None,
                   on_progress=None) -> list:
    """一键签到"""
    activities = get_active_list(session, course_id, class_id)
    results = []

    for act in activities:
        logger.info(f"签到 {act['typeName']} activeId={act['activeId']}")
        result = sign_activity(session, act, manual_code=manual_code,
                               on_progress=on_progress)
        results.append({**act, **result})

        if result["success"]:
            logger.info(f"签到成功 activeId={act['activeId']}")
        else:
            logger.warning(f"签到失败 activeId={act['activeId']}")

        time.sleep(0.5)

    return results


def sign_all_courses(session: ChaoxingSession, courses: list,
                     manual_code: str = None,
                     on_progress=None) -> dict:
    """对所有课程执行签到"""
    total = 0
    success = 0
    failed = 0
    all_results = []

    for c in courses:
        cid = c.get("courseId") or c.get("course_id")
        clid = c.get("classId") or c.get("class_id")
        name = c.get("name") or c.get("course_name", "未知")

        logger.info(f"检查课程: {name}")
        results = check_and_sign(session, cid, clid, manual_code=manual_code,
                                 on_progress=on_progress)

        for r in results:
            total += 1
            if r.get("success"):
                success += 1
            else:
                failed += 1
            all_results.append({"course_name": name, **r})

        time.sleep(0.5)

    return {"total": total, "success": success, "failed": failed, "results": all_results}
