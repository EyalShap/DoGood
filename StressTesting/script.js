import http from 'k6/http';
import { sleep, check } from 'k6';


const loginUrl = "http://dogood.cs.bgu.ac.il:8080/api/users/login"
const getOrganization = (username, orgId) =>
  `http://dogood.cs.bgu.ac.il:8080/api/organizations/getOrganization?orgId=${orgId}&actor=${username}`

export const options = {
  vus: 100,
  duration: "2m"
};

function getRandomOrg(orgAmount) {
  return 1 + Math.floor(Math.random() * orgAmount);
}
export function setup() {
  const tokenDict = new Object();
  for(let i = 0; i < 100; i++){
    const loginPayload = JSON.stringify({
      username: `User${i}`,
      password: "123456"
    })
    const paramsLogin = {
      headers:
      {
        'Content-Type': 'application/json'
      }
    }
  const resLogin = http.post(loginUrl, loginPayload, paramsLogin);
  console.log(`USER ${i} LOGGED IN`)
  tokenDict[`User${i}`] = resLogin.json().data;
  }
  return {tokens: tokenDict}
}

export default function (data) {
  const token = data.tokens[`User${__VU-1}`];
  const paramsGet = {
    headers:
    {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  }
  sleep(3)
  const orgToGet = getRandomOrg(10);
  const resGetOrg = http.get(getOrganization(`User${__VU - 1}`, orgToGet), paramsGet)
  check(resGetOrg, {
    'is get status 200': (r) => r.status === 200,
    'get org successful': (r) => r.json().error === false,
    'valid org': (r) => r.json().data.id === orgToGet
  })
  sleep(3)
}
