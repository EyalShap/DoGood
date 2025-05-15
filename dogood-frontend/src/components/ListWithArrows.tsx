import { useEffect, useState } from "react";
import './../css/ListWithArrows.css'
import './../css/CommonElements.css'
import { useNavigate } from "react-router-dom";
import defaultOrgImage from "/src/assets/defaultOrganizationDog.jpg";

export interface ListItem {
  id: number | string;
  image: string;
  title: string;
  description: string;
}

export interface ListProps {
  data: ListItem[];
  limit: number;
  navigateTo: string;
  clickable?: (id: number | string) => boolean;
  onRemove? : (arg0: string) => Promise<void>;
  isOrgManager? : boolean;
  showArrows? : boolean;
  showResign? :(username: string) => boolean;
  showFire? :(username: string) => boolean;
  showSetAsFounder? :(username: string) => boolean;
  resignHandler? : (arg0: string) => Promise<void>;
  fireHandler? : (arg0: string) => Promise<void>;
  setFounderHandler? : (arg0: string) => Promise<void>;
  setFounderOrPoster? : boolean;
}

const List: React.FC<ListProps> = ({ data, limit, navigateTo, clickable, onRemove, isOrgManager, showArrows = true, showResign, showFire, showSetAsFounder, resignHandler, fireHandler, setFounderHandler, setFounderOrPoster = true }) => {
  const navigate = useNavigate();
  const [startIndex, setStartIndex] = useState(0); 

  const handlePrev = () => {
    if(startIndex == 0) {
      setStartIndex(data.length - 1); 
    }
    else {
      setStartIndex(startIndex - 1);
    }
  };

  const handleNext = () => {
    if(startIndex == data.length - 1) {
      setStartIndex(0); 
    }
    else {
      setStartIndex(startIndex + 1);
    }
  };

  const handleTitleOnClick = (postId: string | number) => {
    navigate(`/${navigateTo}/${postId}`);
  }

  const visibleItems1 = data.slice(startIndex, startIndex + limit);
  const visibleItems2 = data.length > limit ? data.slice(0, limit - visibleItems1.length) : [];
  const visibleItems = visibleItems1.concat(visibleItems2);

  useEffect(() => {
    setStartIndex(0);
  }, [data]); 

  return (
    <div className="listComponent">
      <link
      href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;700&display=swap"
      rel="stylesheet"
      />

      {showArrows && data.length > limit && <button onClick={handlePrev} className="leftArrow"></button>}

      <div className="list">
        {visibleItems.map((item) => (
          <div className="itemAndButton">
          <div key={item.id} className={`listItem ${(clickable ? clickable(item.id) : false) ? "clickable" : "nonClickable"}`} onClick={() => handleTitleOnClick(item.id)}>
            <img
              src={item.image}
              alt={item.title}
              className="listItemImg"
            />
            <h3 className="listItemHeader" style={{overflowWrap: "break-word"}}>{item.title}</h3>
            <p className="listItemDesc" style={{overflowWrap: "break-word"}}>{item.description}</p>
            <div className="listItemButtons">
              {showResign && showResign(item.id.toString()) && resignHandler && <button onClick={(e) => {e.stopPropagation(); resignHandler(item.id.toString());}} className='orangeCircularButton'>Resign</button>}
              {showFire && showFire(item.id.toString()) && fireHandler && <button className='orangeCircularButton' onClick={(e) => {e.stopPropagation(); fireHandler(item.id.toString());}}>X</button>}
              {setFounderOrPoster && showSetAsFounder && showSetAsFounder(item.id.toString()) && setFounderHandler && <button className='orangeCircularButton' onClick={(e) => {e.stopPropagation(); setFounderHandler(item.id.toString());}}>Set As Founder</button>}
              {!setFounderOrPoster && showSetAsFounder && showSetAsFounder(item.id.toString()) && setFounderHandler && <button className='orangeCircularButton' onClick={(e) => {e.stopPropagation(); setFounderHandler(item.id.toString());}}>Set As Poster</button>}
            </div>
          </div>
          {(isOrgManager && onRemove && item.image !== defaultOrgImage) && <button onClick={(e) => {e.stopPropagation(); onRemove(item.image);}} className="removeButton">X</button>}
                                              
          </div>
        ))}
      </div>

      {showArrows && data.length > limit && <button onClick={handleNext} className="rightArrow"></button>}


    </div>
  );
};

export default List;