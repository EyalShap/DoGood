import http from 'k6/http';
import { sleep, check } from 'k6';


const loginUrl = "http://dogood.cs.bgu.ac.il:8080/api/users/login"
    const getOrganization = (username, orgId) => 
    `http://dogood.cs.bgu.ac.il:8080/api/organizations/getOrganization?orgId=${orgId}&actor=${username}`

export const options = {
    stages: [
    { duration: '3s', target: 10 },
    { duration: '3s', target: 50 },
    { duration: '3s', target: 70 },
    { duration: '3s', target: 80 },
    { duration: '3s', target: 95 },
    { duration: '7s', target: 100 },
    { duration: '3s', target: 0 },
  ]
};

function getRandomOrg(orgAmount) {
  return 1+Math.floor(Math.random() * orgAmount);
}

export default function() {
  const loginPayload = JSON.stringify({
        username: `User${__VU-1}`,
        password: "123456"
    })
    const paramsLogin = {
        headers:
        {
            'Content-Type': 'application/json'
        }
    }
    const resLogin = http.post(loginUrl,loginPayload,paramsLogin);
    check(resLogin, {
      'is login status 200': (r) => r.status === 200,
      'login successful': (r) => r.json().error === false
    })
    const token = resLogin.json().data
    const paramsGet = {
        headers:
        {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    }
    sleep(3)
    const orgToGet = getRandomOrg(10);
    const resGetOrg = http.get(getOrganization(`User${__VU-1}`, orgToGet),paramsGet)
    check(resGetOrg, {
      'is get status 200': (r) => r.status === 200,
      'get org successful': (r) => r.json().error === false,
      'valid org': (r) => r.json().data.id === orgToGet
    })
    sleep(3)
}
