import './../css/Homepage.css'
import './../css/CommonElements.css'
import ListWithArrows, { ListItem } from './ListWithArrows';
import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { getAllVolunteeringPosts, getVolunteeringImages, sortByRelevance } from '../api/post_api';
import {PostModel} from "../models/PostModel.ts";


function Homepage() {
    const navigate = useNavigate();
    const [posts, setPosts] = useState<ListItem[]>([]);
    const [addInfoVisible, setAddInfoVisible] = useState(false);
    const isMobile = window.innerWidth <= 768;

    const fetchPosts = async () => {
      try {
        let allPosts = await sortByRelevance(await getAllVolunteeringPosts());
        allPosts = allPosts.slice(0, Math.min(5, allPosts.length));

        const listItemsPromises = allPosts.map(async (post: VolunteeringPostModel) => {
        const images = await getVolunteeringImages(post.volunteeringId);
        const image = images.length > 0 ? images[0] : 'https://cdn.thewirecutter.com/wp-content/media/2021/03/dogharnesses-2048px-6907-1024x682.webp';
            return {
                id: (post as PostModel).id,
                image: image,
                title: post.title,
                description: post.description,
            };
      });
      const listItems = await Promise.all(listItemsPromises);
        setPosts(listItems);
      } catch (e) {
        alert(e);
      }
    }
    
    useEffect(() => {
      fetchPosts();
    }, [])

    const handleBrowseOnClick = () => {
        navigate(`/volunteeringPostList`);
    }

    const addOnClick = async () => {
      setAddInfoVisible(true);
    }

    const handleCancelOnClick = async () => {
      setAddInfoVisible(false);
    }

    const quickstartOnClick = () => {
      navigate('/createOrganization/-1/1');
    }
    
    return (
        <div className = "generalPageDiv">
            <link
            href="https://fonts.googleapis.com/css2?family=Lobster&display=swap"
            rel="stylesheet"
            />
            <link
            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
            rel="stylesheet"
            />
            <div className="headers">
                <h1 className = "bigHeader">Discover Meaningful Volunteering Opportunities</h1>
                <h1 className = "smallHeader">​Find the perfect volunteering opportunities - start making a real impact today</h1>
                <button id = "browseButton" className = "orangeCircularButton" onClick={handleBrowseOnClick}>BROWSE VOLUNTEERING OPPORTUNITIES</button>
                <button id = "browseButton" className = "orangeCircularButton" onClick={addOnClick}>ADD YOUR OWN VOLUNTEERING OPPORTUNITY</button>
            
                {addInfoVisible && (
                        <div className="popup-window">
                            <div className="popup-header">
                            <span className="popup-title">Add Volunteering Opportunity</span>
                            <button className="cancelButton" onClick={handleCancelOnClick}>
                                X
                            </button>
                            </div>
                            <div className="popup-body">
                                <p style={{fontSize:'1.2rem'}}>Step 1: Create your organization.</p>
                                <p style={{fontSize:'1.2rem'}}>Step 2: Add your volunteering to the organization.</p>
                                <p style={{fontSize:'1.2rem'}}>Step 3: Post your volunteering to reach volunteers.</p>
                                <button className = "orangeCircularButton" onClick={quickstartOnClick}>Start</button>
                            </div>
                        </div>
                    )}
            </div>

            <div className='generalList'>
                <h1 className='recommendedDesc'>Handpicked Volunteering Opportunities, Just For You</h1>
                <ListWithArrows data={posts} limit = {!isMobile ? 3 : posts.length} navigateTo = 'volunteeringPost' clickable={() => true} showArrows={!isMobile}></ListWithArrows>
            </div>
        </div>
    );
  }
  
  export default Homepage;
  