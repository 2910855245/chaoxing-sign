#!/usr/bin/env python3
"""
学习通签到自动化测试脚本

流程:
  1. 教师账号创建并发布签到
  2. 学生账号自动签到
"""
import sys
import time

sys.stdout.reconfigure(encoding='utf-8')

from chaoxing.session import ChaoxingSession
from chaoxing.teacher import publish_sign, get_active_list_teacher, end_sign_active
from chaoxing.signin import get_active_list, sign_activity


# 账号配置
TEACHER = {'username': '19136434661', 'password': 'woainima123'}
STUDENT = {'username': '19102804734', 'password': 'Lsq200671'}

# 课程配置
COURSE_ID = '264381020'
CLASS_ID = '148304228'


def print_header(title):
    print(f'\n{"="*50}')
    print(f'  {title}')
    print(f'{"="*50}')


def print_step(step, msg):
    print(f'  [{step}] {msg}')


def test_full_flow():
    """完整测试流程"""
    print_header('学习通签到自动化测试')

    # === 教师端 ===
    print_header('教师端：发布签到')
    teacher = ChaoxingSession()
    if not teacher.login(TEACHER['username'], TEACHER['password']):
        print('  ❌ 教师登录失败')
        return
    print_step(1, f'登录成功: {teacher.uid}')

    # 一键发布签到
    print_step(2, '发布签到...')
    result = publish_sign(teacher, COURSE_ID, CLASS_ID,
                          title='自动化测试签到', duration_minutes=10)
    if not result.get('success'):
        print(f'  ❌ 发布失败: {result.get("message")}')
        return

    active_id = result.get('active_id')
    print_step(3, f'发布成功! 活动ID: {active_id}')

    # === 学生端 ===
    print_header('学生端：签到')
    student = ChaoxingSession()
    if not student.login(STUDENT['username'], STUDENT['password']):
        print('  ❌ 学生登录失败')
        return
    print_step(1, f'登录成功: {student.uid}')

    # 获取签到活动
    print_step(2, '获取签到活动...')
    activities = get_active_list(student, COURSE_ID, CLASS_ID)
    print(f'       共 {len(activities)} 个活动')

    for act in activities[:3]:
        status = '待签' if act['status'] == 0 else '已签' if act['status'] == 1 else '已过期'
        print(f'       - {act["typeName"]} | {status} | id={act["activeId"]}')

    # 找到待签到的活动
    pending = [a for a in activities if a['status'] == 0 and a.get('userStatus') != 1]
    if not pending:
        print_step(3, '没有待签到活动（可能已自动签到）')
    else:
        act = pending[0]
        print_step(3, f'执行签到: {act["typeName"]}')
        result = sign_activity(student, act)
        success = result.get('success')
        msg = result.get('message', '')
        if success:
            print(f'       ✅ 签到成功: {msg}')
        else:
            print(f'       ❌ 签到失败: {msg}')

    print_header('测试完成')


def test_publish_only():
    """仅发布签到"""
    print_header('教师端：发布签到')
    teacher = ChaoxingSession()
    if not teacher.login(TEACHER['username'], TEACHER['password']):
        print('  ❌ 登录失败')
        return

    result = publish_sign(teacher, COURSE_ID, CLASS_ID,
                          title='手动测试签到', duration_minutes=10)
    if result.get('success'):
        print(f'  ✅ 发布成功! 活动ID: {result.get("active_id")}')
    else:
        print(f'  ❌ 发布失败: {result.get("message")}')


def test_sign_only():
    """仅学生签到"""
    print_header('学生端：签到')
    student = ChaoxingSession()
    if not student.login(STUDENT['username'], STUDENT['password']):
        print('  ❌ 登录失败')
        return

    activities = get_active_list(student, COURSE_ID, CLASS_ID)
    pending = [a for a in activities if a['status'] == 0 and a.get('userStatus') != 1]

    if not pending:
        print('  没有待签到活动')
        return

    for act in pending:
        print(f'  签到: {act["typeName"]} (id={act["activeId"]})')
        result = sign_activity(student, act)
        if result.get('success'):
            print(f'    ✅ 成功')
        else:
            print(f'    ❌ 失败: {result.get("message")}')


def main():
    if len(sys.argv) > 1:
        cmd = sys.argv[1]
        if cmd == 'publish':
            test_publish_only()
        elif cmd == 'sign':
            test_sign_only()
        elif cmd == 'full':
            test_full_flow()
        else:
            print('用法: python test_auto_sign.py [publish|sign|full]')
    else:
        test_full_flow()


if __name__ == '__main__':
    main()
