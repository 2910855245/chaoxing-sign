"""学习通扫描入口 — 爬取 → 清洗 → 筛选，统一返回标准化格式"""
import concurrent.futures
from loguru import logger

from chaoxing.session import ChaoxingSession
from chaoxing.scanner.crawler import (
    fetch_course_list,
    fetch_knowledge_list,
    fetch_points,
)
from chaoxing.scanner.cleaner import clean_courses, clean_course_full
from chaoxing.scanner.task_filter import get_actionable_tasks, get_done_courses


def _process_single_course(session: ChaoxingSession, c: dict) -> dict:
    """处理单门课程"""
    cid = c["course_id"]
    clid = c["class_id"]

    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        f_points = pool.submit(fetch_points, session, cid, clid)
        f_raw_points = pool.submit(fetch_knowledge_list, session, cid, clid)
        points = f_points.result()
        raw_points = f_raw_points.result()

    cleaned = clean_course_full(
        {"courseId": cid, "classId": clid, "name": c["course_name"]},
        raw_points,
        points,
    )
    cleaned["teacher"] = c.get("teacher", "")
    return cleaned


def _fetch_cpi_map(session: ChaoxingSession) -> dict:
    """获取 courseId → cpi 映射"""
    cpi_map = {}
    try:
        resp = session.get('https://mooc1-api.chaoxing.com/mycourse/backclazzdata?view=json&rss=1')
        for ch in resp.json().get('channelList', []):
            content = ch.get('content', {})
            if isinstance(content, dict):
                for cr in content.get('course', {}).get('data', []):
                    cpi_map[str(cr.get('id', ''))] = str(ch.get('cpi', ''))
    except Exception as e:
        logger.warning(f"获取cpi映射失败 error={str(e)}")
    return cpi_map


def scan_chaoxing(session: ChaoxingSession) -> dict:
    """扫描学习通全部课程签到状态

    返回: {website_id, name, status, student_name, courses, tasks}
    """
    user_info = session.get_user_info()
    student_name = user_info.get("name", "")

    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        f_courses = pool.submit(fetch_course_list, session)
        f_cpi = pool.submit(_fetch_cpi_map, session)
        raw_courses = f_courses.result()
        cpi_map = f_cpi.result()

    if not raw_courses:
        return {
            "website_id": 4,
            "name": "学习通",
            "status": "error",
            "error": "获取课程列表失败",
            "courses": [],
            "tasks": [],
        }

    active_courses = clean_courses(raw_courses)
    ended_count = len(raw_courses) - len(active_courses)
    if ended_count:
        logger.info(f"排除已结束课程 ended={ended_count} active={len(active_courses)}")

    courses = []
    if len(active_courses) <= 2:
        for c in active_courses:
            courses.append(_process_single_course(session, c))
    else:
        with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
            futures = {pool.submit(_process_single_course, session, c): c for c in active_courses}
            for future in concurrent.futures.as_completed(futures):
                try:
                    courses.append(future.result())
                except Exception as e:
                    c = futures[future]
                    logger.error(f"课程处理失败 {c.get('course_name')}: {e}")

    tasks = get_actionable_tasks(courses)
    done = get_done_courses(courses)

    logger.info("扫描完成", total=len(courses), actionable=len(tasks), done=len(done))

    total_points = 0
    for c in courses:
        pts = c.get('points', {})
        if pts and pts.get('total') is not None:
            total_points = max(total_points, pts['total'])

    return {
        "website_id": 4,
        "name": "学习通",
        "status": "ok",
        "student_name": student_name,
        "school_name": user_info.get("school_name", ""),
        "student_code": user_info.get("student_code", ""),
        "points_total": total_points,
        "points_target": 200,
        "courses": courses,
        "tasks": tasks,
    }
