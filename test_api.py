import requests

api_key = "b1631ef776724f03a6925ba7b6daea99"
base_url = "https://open.neis.go.kr/hub"

# 1. School Info
res = requests.get(f"{base_url}/schoolInfo?KEY={api_key}&Type=json&pIndex=1&pSize=10&SCHUL_NM=서울과")
print("School Info:")
print(res.json())

# 2. Class Info (for a specific school, need ATPT_OFCDC_SC_CODE and SD_SCHUL_CODE)
# Let's get "서울과학고등학교"
code1 = "B10" # Seoul
code2 = "7010059" # Seoul Science High School
res2 = requests.get(f"{base_url}/classInfo?KEY={api_key}&Type=json&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE={code1}&SD_SCHUL_CODE={code2}")
print("\nClass Info:")
print(res2.json())

# 3. Timetable
res3 = requests.get(f"{base_url}/hisTimetable?KEY={api_key}&Type=json&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE={code1}&SD_SCHUL_CODE={code2}&AY=2024&SEM=1&ALL_TI_YMD=20240416")
print("\nTimetable:")
print(res3.json())
