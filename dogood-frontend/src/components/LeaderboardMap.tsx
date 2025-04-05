import './../css/CommonElements.css'
import './../css/Leaderboards.css'
import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Leaderboard } from '../models/Leaderboard';
import { getUserByUsername, leaderboard } from '../api/user_api';
import defaultProfilePic from '/src/assets/defaultProfilePic.jpg';
import User from '../models/UserModel';


function LeaderboardMap() {
    const navigate = useNavigate();
    const [leaderboardData, setLeaderboardData] = useState<Leaderboard>({});
    const [userImages, setUserImages] = useState<{ [key: string]: string }>({});

    const fetchLeaderboard = async () => {
      try {
        let res = await leaderboard();
        setLeaderboardData(res);
        console.log(Object.entries(leaderboardData));
        
        for (let [username, score] of Object.entries(leaderboardData)) {
            try {
                const user: User = await getUserByUsername(username);
                const image: string = (user.profilePicUrl !== null && user.profilePicUrl !== "") ? user.profilePicUrl : defaultProfilePic;
                                        
                setUserImages((prev) => ({
                    ...prev,
                    [username]: image,
                }));
            } catch (e) {
                alert(e);
            }
        }

      } catch (e) {
        alert(e);
      }
    }

    
    useEffect(() => {
        fetchLeaderboard();
    }, [])

    const handleUserOnClick = async (username: string | number) => {
        navigate(`/profile/${username}`);
    }
    
    return (
        <div className='generalPageDiv'>
            <div className='headers'>
                <h1 className='bigHeader'>Do Good's Leaderboard</h1>
                <h2 className='smallHeader'>Challenge your friends and climb to the top to become the ultimate volunteer!</h2>
            </div>
            <table className="leaderboard">
                <thead>
                    <tr className='leaderboardHeaders'>
                        <th>Username</th>
                        <th>Score</th>
                    </tr>
                </thead>
                <tbody>
                    {Object.entries(leaderboardData!).reverse().map(([user, score]) => (
                        <tr key={user} className='leaderboardItem' onClick={() => handleUserOnClick(user)}>
                            <td className='username'>
                                <div style={{display: 'flex', flexDirection: 'row', gap: '20px'}}>
                                    <img src={userImages[user]} style={{width: '50px', height:'50px', borderRadius:'50%'}}></img>
                                    <p>{user}</p>
                                </div>
                            </td>
                            <td>{score.toString() === score.toFixed(0) ? score : score.toFixed(1)}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
  }
  
  export default LeaderboardMap;
  