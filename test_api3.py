import requests

api_key = "b1631ef776724f03a6925ba7b6daea99"
base_url = "https://open.neis.go.kr/hub"

# Let's see recent dates for any school
res = requests.get(f"{base_url}/hisTimetable?KEY={api_key}&Type=json&pIndex=1&pSize=10&ALL_TI_YMD=20240416")
data = res.json()
if 'hisTimetable' in data:
    print(data)
else:
    print(data)
