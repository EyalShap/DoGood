import './../css/CommonElements.css'
import './../css/Leaderboards.css'
import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Leaderboard } from '../models/Leaderboard';
import { leaderboard } from '../api/user_api';

function LeaderboardMap() {
    const navigate = useNavigate();
    const [leaderboardData, setLeaderboardData] = useState<Leaderboard>({});

    const fetchLeaderboard = async () => {
      try {
        let res = await leaderboard();
        setLeaderboardData(res);
      } catch (e) {
        alert(e);
      }
    }
    
    useEffect(() => {
        fetchLeaderboard();
    }, [])
    
    return (
        <div className='generalPageDiv'>
            <div className='headers'>
                <h1 className='bigHeader'>Do Good's Leaderboard</h1>
                <h2 className='smallHeader'>Challenge your friends and climb to the top to become the ultimate volunteer!</h2>
            </div>
            <table>
                <thead>
                    <tr className='leaderboardHeaders'>
                        <th>Username</th>
                        <th>Score</th>
                    </tr>
                </thead>
                <tbody>
                    {Object.entries(leaderboardData!).map(([user, score]) => (
                        <tr key={user} className='leaderboardItem'>
                            <td>{user}</td>
                            <td>{score}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
  }
  
  export default LeaderboardMap;
  