import requests

api_key = "b1631ef776724f03a6925ba7b6daea99"
base_url = "https://open.neis.go.kr/hub"

# Let's get hisTimetable for today or nearby dates for some random school to see if any data is there.
# Let's try 20240401 to 20240405
for day in range(1, 6):
    date_str = f"202404{day:02d}"
    res = requests.get(f"{base_url}/hisTimetable?KEY={api_key}&Type=json&pIndex=1&pSize=10&ATPT_OFCDC_SC_CODE=B10&SD_SCHUL_CODE=7010059&AY=2024&SEM=1&ALL_TI_YMD={date_str}")
    data = res.json()
    if 'hisTimetable' in data:
        print(f"Data found for {date_str}")
        print(data)
        break
    else:
        print(f"No data for {date_str}")
