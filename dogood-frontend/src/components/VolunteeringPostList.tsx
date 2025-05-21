import { useEffect, useState } from 'react'
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { filterVolunteeringPosts, filterVolunteerPosts, getAllOrganizationNames, getAllPostsCities, getAllVolunteeringNames, getAllVolunteeringPosts, getAllVolunteeringPostsCategories, getAllVolunteeringPostsSkills, getAllVolunteerPosts, getAllVolunteerPostsCategories, getAllVolunteerPostsSkills, getVolunteeringImages, searchByKeywords, sortByLastEditTime, sortByPopularity, sortByPostingTime, sortByRelevance } from '../api/post_api';
import { useNavigate } from 'react-router-dom';
import './../css/VolunteeringPostList.css'
import MultipleSelectDropdown from './MultipleSelectDropdown';
import Select from 'react-select';
import { SingleValue } from 'react-select';
import ListWithPlus from './ListWithPlus';
import { ListItem } from './ListWithArrows';
import { PostModel } from '../models/PostModel';
import { VolunteerPostModel } from '../models/VolunteerPostModel';
import { Switch } from '@mui/material';
import defaultVolunteeringPic from '/src/assets/defaultVolunteeringDog.webp';
import defaultVolunteerPostPic from '/src/assets/defaultVolunteerPostDog.jpg';

function VolunteeringPostList() {
    const isMobile = window.innerWidth <= 768;
    const navigate = useNavigate();
    const[isVolunteeringPosts, setIsVolunteeringPosts] = useState<boolean>(true);
    const [posts, setPosts] = useState<PostModel[]>([]);
    const [searchedPosts, setSearchedPosts] = useState<PostModel[]>([]);
    //const [seacrhedVolunteerPosts, setSearchedVolunteerPosts] = useState<VolunteerPostModel[]>([]);
    //const [filteredPosts, setFilteredPosts] = useState<PostModel[]>([]);
    const [allVolunteeringPosts, setAllVolunteeringPosts] = useState<VolunteeringPostModel[]>([]);
    const [allVolunteerPosts, setAllVolunteerPosts] = useState<VolunteerPostModel[]>([]);
    const [postsListItems, setPostsListItems] = useState<ListItem[]>([]);
    const [search, setSearch] = useState("");
    const[sortFunction, setSortFunction] = useState<SingleValue<{ value: string; label: string }>>(null);
    const[allCategories, setAllCategories] = useState<string[]>([]);
    const[allSkills, setAllSkills] = useState<string[]>([]);
    const[allCities, setAllCities] = useState<string[]>([]);
    const[allOrganizationNames, setAllOrganizationNames] = useState<string[]>([]);
    const[allVolunteeringNames, setAllVolunteeringNames] = useState<string[]>([]);
    const[selectedCategories, setSelectedCategories] = useState<string[]>([]);
    const[selectedSkills, setSelectedSkills] = useState<string[]>([]);
    const[selectedCities, setSelectedCities] = useState<string[]>([]);
    const[selectedOrganizationNames, setSelectedOrganizationNames] = useState<string[]>([]);
    const[selectedVolunteeringNames, setSelectedVolunteeringNames] = useState<string[]>([]);
    const[selectedIsMyPosts, setSelectedIsMyPosts] = useState<string[]>([]);
    const [postImages, setPostImages] = useState<Map<number, string>>(new Map());
    const[showFilter, setShowFilter] = useState<boolean>(!isMobile);

    const fetchPosts = async () => {
        try {
            if(isVolunteeringPosts) {
                let fetchedPosts: VolunteeringPostModel[] = [];

                if(allVolunteeringPosts.length !== 0) {
                    fetchedPosts = allVolunteeringPosts;
                }
                else {
                    fetchedPosts = await sortByRelevance(await getAllVolunteeringPosts());
                    setAllVolunteeringPosts(fetchedPosts);
                    setSearchedPosts(fetchedPosts);
                }
                setPosts(fetchedPosts);

                const imagesArray = await Promise.all(
                    fetchedPosts.map(post => getVolunteeringImages(post.volunteeringId))
                );
        
                fetchedPosts.forEach((post, index) => {
                    const images = imagesArray[index];
                    const firstImage = images.length > 0 ? images[0] : defaultVolunteeringPic;
                    postImages.set(post.id, firstImage);
                });

                let listItems = convertToListItems(fetchedPosts);
                setPostsListItems(listItems);

                let allCategories = await getAllVolunteeringPostsCategories();
                setAllCategories(allCategories);

                let allSkills = await getAllVolunteeringPostsSkills();
                setAllSkills(allSkills);

                let allCities = await getAllPostsCities();
                setAllCities(allCities);

                let allOrganizationNames = await getAllOrganizationNames();
                setAllOrganizationNames(allOrganizationNames);

                let allVolunteeringNames = await getAllVolunteeringNames();
                setAllVolunteeringNames(allVolunteeringNames);
            }
            else {
                let fetchedPosts: VolunteerPostModel[] = [];
                if(allVolunteerPosts.length !== 0) {
                    fetchedPosts = allVolunteerPosts;
                }
                else {
                    fetchedPosts = await getAllVolunteerPosts();
                    setAllVolunteerPosts(fetchedPosts);
                    setSearchedPosts(fetchedPosts);
                }
                setPosts(fetchedPosts);
        
                fetchedPosts.forEach((post, index) => {
                    const firstImage = post.images.length > 0 ? post.images[0] : defaultVolunteerPostPic;
                    postImages.set(post.id, firstImage);
                });

                let listItems = convertToListItems(fetchedPosts);
                setPostsListItems(listItems);

                let allCategories = await getAllVolunteerPostsCategories();
                setAllCategories(allCategories);

                let allSkills = await getAllVolunteerPostsSkills();
                setAllSkills(allSkills);
            }
            
            
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchPosts();
    }, [isVolunteeringPosts])

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearch(event.target.value);
    };

    const convertToListItems = (posts: PostModel[]) => {
        const listItems: ListItem[] = posts.map((post) => ({
            id: post.id,
            image: postImages.get(post.id) ?? defaultVolunteeringPic, 
            title: post.title,  
            description: post.description, // assuming 'summary' is a short description
        }));
        return listItems;
    }

    const handleSearchOnClick = async () => {
        try {
            let res : PostModel[] = [];
            if(isVolunteeringPosts) {
                if(search.trim() === "") {
                    res = allVolunteeringPosts;
                }
                res = await searchByKeywords(search, allVolunteeringPosts, isVolunteeringPosts);
                setSearchedPosts(res);
                let resVolunteeringPosts : number[] = res.map(resPost => resPost.id);
                res = await filterVolunteeringPosts(selectedCategories, selectedSkills, selectedCities, selectedOrganizationNames, selectedVolunteeringNames, resVolunteeringPosts, selectedIsMyPosts[0] === 'My Posts' ? true : false);
            }
            else {
                if(search.trim() === "") {
                    console.log(allVolunteerPosts);
                    res = allVolunteerPosts;
                }
                res = await searchByKeywords(search, allVolunteerPosts, isVolunteeringPosts);                
                setSearchedPosts(res);
                let resVolunteerPosts : number[] = res.map(resPost => resPost.id);
                res = await filterVolunteerPosts(selectedCategories, selectedSkills, resVolunteerPosts, selectedIsMyPosts[0] === 'My Posts' ? true : false);
            }
            
            setPosts(res);
            setPostsListItems(convertToListItems(res));
            
        }
        catch(e) {
            alert(e);
        }
    }

    const handleSelectionChange = async (selectedOption: SingleValue<{ value: string; label: string }>) => {
        setSortFunction(selectedOption);
        
        if(selectedOption === null || selectedOption.value === 'relevance') {
            let sorted: VolunteeringPostModel[] = await sortByRelevance(posts as VolunteeringPostModel[]);
            setPosts(sorted);
            setPostsListItems(convertToListItems(sorted));
        }
        else if(selectedOption.value === 'popularity') {
            let sorted: VolunteeringPostModel[] = await sortByPopularity(posts as VolunteeringPostModel[]);
            setPosts(sorted);
            setPostsListItems(convertToListItems(sorted));
        }
        else if(selectedOption.value === 'posting time') {
            let sorted: PostModel[] = await sortByPostingTime(posts);
            setPosts(sorted);
            setPostsListItems(convertToListItems(sorted));
        }
        else if(selectedOption.value === 'last edit time') {
            let sorted: PostModel[] = await sortByLastEditTime(posts);
            setPosts(sorted);
            setPostsListItems(convertToListItems(sorted));
        }
    };


    const handleIsMyPostsSelectionChange = async (selectedValues: string[]) => {
        let prevValues = selectedIsMyPosts;
        console.log(prevValues);
        console.log(selectedValues);
        let newValues = selectedValues.filter(item => !prevValues.includes(item));
        setSelectedIsMyPosts(newValues);
        console.log(newValues);
        
        let newValue = newValues.length > 0 ? newValues[0] === 'My Posts' : false;
        console.log(newValue);
        await filter(selectedCategories, selectedSkills, selectedCities, selectedOrganizationNames, selectedVolunteeringNames, newValue);
    };

    const filter = async(categories: string[], skills: string[], cities: string[], organizationNames: string[], volunteeringNames: string[], isMyPosts: boolean) => {
        let filtered : PostModel[] = [];
        console.log("hhihihi");
        console.log(isMyPosts);
        if(isVolunteeringPosts) {
            if(categories.length === 0 && skills.length === 0 && cities.length === 0 && organizationNames.length === 0 && volunteeringNames.length === 0 && !isMyPosts) {
                //filtered = allVolunteeringPosts;
                filtered = searchedPosts;
            }
            else {
                let postIds : number[] = searchedPosts.map(post => post.id);
                console.log(true);
                filtered = await filterVolunteeringPosts(categories, skills, cities, organizationNames, volunteeringNames, postIds, isMyPosts);
            }
        }
        else {
            if(categories.length === 0 && skills.length === 0 && !isMyPosts) {
                //filtered = allVolunteerPosts;
                filtered = searchedPosts;
            }
            else {
                let postIds : number[] = searchedPosts.map(post => post.id);
                filtered = await filterVolunteerPosts(categories, skills, postIds, isMyPosts);
            }
        }
        setPosts(filtered);
        setPostsListItems(convertToListItems(filtered));
    }

    const handleSelectedCategoriesChange = async (selectedValues: string[]) => {
        setSelectedCategories(selectedValues);
        
        let isMyPosts = isMyPostsOptions.length > 0 ? isMyPostsOptions[0] === 'My Posts' : false;
        await filter(selectedValues, selectedSkills, selectedCities, selectedOrganizationNames, selectedVolunteeringNames, isMyPosts);
    };

    const handleSelectedSkillsChange = async (selectedValues: string[]) => {
        setSelectedSkills(selectedValues);
        let isMyPosts = isMyPostsOptions.length > 0 ? isMyPostsOptions[0] === 'My Posts' : false;
        await filter(selectedCategories, selectedValues, selectedCities, selectedOrganizationNames, selectedVolunteeringNames, isMyPosts);
    };

    const handleSelectedCitiesChange = async (selectedValues: string[]) => {
        setSelectedCities(selectedValues);
        let isMyPosts = isMyPostsOptions.length > 0 ? isMyPostsOptions[0] === 'My Posts' : false;
        await filter(selectedCategories, selectedSkills, selectedValues, selectedOrganizationNames, selectedVolunteeringNames, isMyPosts);
    };

    const handleSelectedOrganizationsChange = async (selectedValues: string[]) => {
        setSelectedOrganizationNames(selectedValues);
        let isMyPosts = isMyPostsOptions.length > 0 ? isMyPostsOptions[0] === 'My Posts' : false;
        await filter(selectedCategories, selectedSkills, selectedCities, selectedValues, selectedVolunteeringNames, isMyPosts);
    };

    const handleSelectedVolunteeringsChange = async (selectedValues: string[]) => {
        setSelectedVolunteeringNames(selectedValues);
        let isMyPosts = isMyPostsOptions.length > 0 ? isMyPostsOptions[0] === 'My Posts' : false;
        await filter(selectedCategories, selectedSkills, selectedCities, selectedOrganizationNames, selectedValues, isMyPosts);
    };

    const sortingOptions = 
        isVolunteeringPosts ? 
            [
                { value: 'relevance', label: 'Relevance' },
                { value: 'popularity', label: 'Popularity' },
                { value: 'posting time', label: 'Posting time' },
                { value: 'last edit time', label: 'Last edit time' },
            ] 
            :
            [
                { value: 'posting time', label: 'Posting time' },
                { value: 'last edit time', label: 'Last edit time' },
            ];

        const isMyPostsOptions = 
            [
                'All Posts', 'My Posts'
            ];

    const toggleSwitch = () => {
        setIsVolunteeringPosts(!isVolunteeringPosts);
    }

    const handleCreateNewVolunteerPostOnClick = () => {
        navigate('/createVolunteerPost/-1');
    }

    return (
        <div className='generalPageDiv'>
            <link
            href="https://fonts.googleapis.com/css2?family=Lobster&display=swap"
            rel="stylesheet"
            />
            <div className="Posts">
                <div className='postListHeaders'>
                <h1 className = "bigHeader">Your Opportunity To Volunteer</h1>


                <div className="switch-container">
                    <label className="switch-label">Volunteer Posts</label>
                    <Switch className='switch' defaultChecked onChange={toggleSwitch}/>
                    <label className="switch-label">Volunteering Posts</label>
                </div>

                {!isVolunteeringPosts && <button className='orangeCircularButton' onClick={handleCreateNewVolunteerPostOnClick}>Create New Volunteer Post</button>}

                
                </div>
                <div className="search">
                        
                    <input id = "searchTextbox"
                        type="text"
                        value={search}
                        onChange={handleSearchChange}
                        placeholder = "Search..."
                    />

                    <button className='orangeCircularButton' onClick={handleSearchOnClick}>Search</button>

                    <Select className='sortDropDown'
                        value={sortFunction}
                        onChange={handleSelectionChange}
                        options={sortingOptions}
                        placeholder="Sort By"
                    />

                {isMobile && <img
                    src="https://static.thenounproject.com/png/4800805-200.png"
                    className="showFilter"
                    onClick = {() => setShowFilter(!showFilter)}
                />}
                    
                </div>

                
                {showFilter && <div className = "filter">
                    <MultipleSelectDropdown label={'Categories'} options={allCategories} onChange={handleSelectedCategoriesChange}></MultipleSelectDropdown>
                    <MultipleSelectDropdown label={'Skills'} options={allSkills} onChange={handleSelectedSkillsChange}></MultipleSelectDropdown>
                    {isVolunteeringPosts && <MultipleSelectDropdown label={'Cities'} options={allCities} onChange={handleSelectedCitiesChange}></MultipleSelectDropdown>}
                    {isVolunteeringPosts && <MultipleSelectDropdown label={'Organizations'} options={allOrganizationNames} onChange={handleSelectedOrganizationsChange}></MultipleSelectDropdown>}
                    {isVolunteeringPosts && <MultipleSelectDropdown label={'Volunteerings'} options={allVolunteeringNames} onChange={handleSelectedVolunteeringsChange}></MultipleSelectDropdown>}
                    <MultipleSelectDropdown label={'My Posts'} options={isMyPostsOptions} onChange={handleIsMyPostsSelectionChange}></MultipleSelectDropdown>
                </div>}

                {postsListItems.length > 0 ?
                    <ListWithPlus data={postsListItems} limit = {9} navigateTo={isVolunteeringPosts ? 'volunteeringPost' : 'volunteerPost'}></ListWithPlus>
                    :
                    <p className='noPosts'>No posts available.</p>
                }

            </div>
        </div>
    )
}

export default VolunteeringPostList;